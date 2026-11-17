package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.categories.Category;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.util.SolrTestHelperImpl;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests GET /apix/v1/category/aspect/{qname} against Alfresco's bootstrap category tree (cm:generalclassifiable).
 * Read-only: it relies on seeded data and does not create, modify or delete repository content.
 */
public class CategoriesTest extends RestV1BaseTest {

    private static final String GENERAL_CLASSIFIABLE = "cm:generalclassifiable";

    private final SolrTestHelperImpl solrHelper;

    public CategoriesTest() {
        solrHelper = getBean(SolrTestHelperImpl.class);
    }

    @Before
    public void setup() throws InterruptedException {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Alfresco's category lookups are search-index queries; a freshly started environment may not have indexed
        // the bootstrap categories yet.
        solrHelper.waitForTransactionSync();
    }

    @Test
    public void testGetCategoriesForAspect() throws IOException {
        String url = makeAlfrescoBaseurlAdmin() + "/apix/v1/category/aspect/" + GENERAL_CLASSIFIABLE;
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        byte[] body = EntityUtils.toByteArray(httpResponse.getEntity());

        // On failure, include the body: Alfresco's error response carries the server-side exception message.
        assertEquals("Unexpected status, response body: " + new String(body, StandardCharsets.UTF_8),
                200, httpResponse.getStatusLine().getStatusCode());
        // Same headers as the original Alfresco MVC implementation: no charset parameter, explicit no-cache.
        Header contentType = httpResponse.getFirstHeader("Content-Type");
        assertNotNull("Response has no Content-Type header", contentType);
        assertEquals("application/json", contentType.getValue());
        Header[] cacheControl = httpResponse.getHeaders("Cache-Control");
        assertEquals(1, cacheControl.length);
        assertEquals("no-cache", cacheControl[0].getValue());

        // Jackson detects the encoding from the bytes (UTF-8), independent of any response header.
        JsonNode root = new ObjectMapper().readTree(body);
        assertTrue("Response should be a JSON object", root.isObject());
        assertArrayEquals("Response should only contain the 'categories' wrapper",
                new String[]{"categories"}, fieldNames(root).toArray());

        JsonNode categories = root.get("categories");
        assertTrue("'categories' should be an array", categories.isArray());
        assertFalse("'categories' should not be empty", categories.isEmpty());
        for (JsonNode category : categories) {
            assertCategoryShape(category);
        }

        JsonNode languages = findByName(categories, "Languages");
        assertNotNull("Expected root category 'Languages'", languages);
        assertEquals("/cm:categoryRoot/cm:generalclassifiable/cm:Languages", languages.get("qnamePath").asText());
        assertTrue(languages.get("noderef").asText().startsWith("workspace://SpacesStore/"));

        JsonNode regions = findByName(categories, "Regions");
        assertNotNull("Expected root category 'Regions'", regions);
        JsonNode regionChildren = regions.get("subcategories");
        assertFalse("'Regions' should have subcategories", regionChildren.isEmpty());
        for (JsonNode child : regionChildren) {
            assertCategoryShape(child);
        }

        // The endpoint should expose exactly the category roots that the Java service returns.
        List<Category> serviceRoots = getBean(ICategoryService.class).getCategoryTree(new QName(GENERAL_CLASSIFIABLE));
        Set<String> expectedRootNodeRefs = new TreeSet<>();
        serviceRoots.forEach(category -> expectedRootNodeRefs.add(category.getNoderef()));
        Set<String> actualRootNodeRefs = new TreeSet<>();
        categories.forEach(category -> actualRootNodeRefs.add(category.get("noderef").asText()));
        assertEquals(expectedRootNodeRefs, actualRootNodeRefs);
    }

    private static void assertCategoryShape(JsonNode category) {
        for (String field : new String[]{"noderef", "name", "qnamePath", "subcategories"}) {
            assertTrue("Category " + category + " is missing '" + field + "'", category.has(field));
        }
        assertTrue(category.get("subcategories").isArray());
        assertFalse(category.get("name").asText().isEmpty());
    }

    private static JsonNode findByName(JsonNode categories, String name) {
        for (JsonNode category : categories) {
            if (name.equals(category.get("name").asText())) {
                return category;
            }
        }
        return null;
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
