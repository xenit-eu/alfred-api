package eu.xenit.alfred.api.rest.v0.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.v1.tests.RestV1BaseTest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests GET /apix/classification/{aspectQName} (v0, no version in the URL) against Alfresco's bootstrap category tree
 * (cm:generalclassifiable). Read-only: it relies on seeded data and does not change repository content.
 */
public class ClassificationTest extends RestV1BaseTest {

    private static final String GENERAL_CLASSIFIABLE = "cm:generalclassifiable";
    private static final String GENERAL_CLASSIFIABLE_FULL =
            "{http://www.alfresco.org/model/content/1.0}generalclassifiable";

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetClassificationForAspect() throws IOException {
        JsonNode categories = getClassification(GENERAL_CLASSIFIABLE);

        // v0 returns the list itself, unlike v1's {"categories": [...]} wrapper.
        assertTrue("Response should be a JSON array", categories.isArray());
        Set<String> names = new TreeSet<>();
        categories.forEach(category -> names.add(category.get("name").asText()));
        assertTrue("Expected root categories 'Languages' and 'Regions' in " + names,
                names.contains("Languages") && names.contains("Regions"));

        // The endpoint should expose exactly the category roots that the Java service returns.
        assertEquals(serviceRootNodeRefs(), nodeRefs(categories));
    }

    @Test
    public void testGetClassificationForFullQName() throws IOException {
        // The full QName contains '/', which the MVC path variable did not accept; the native web script does.
        // The slashes are sent as-is: Tomcat rejects an encoded slash (%2F) before the request reaches Alfresco.
        String encoded = URLEncoder.encode(GENERAL_CLASSIFIABLE_FULL, StandardCharsets.UTF_8)
                .replace("%2F", "/");

        assertEquals(serviceRootNodeRefs(), nodeRefs(getClassification(encoded)));
    }

    /**
     * Uses java.net.http instead of Apache HttpClient, which normalizes the path and would collapse the "//" of a
     * full QName's namespace ("http%3A//www...") into a single slash.
     */
    private JsonNode getClassification(String aspectQName) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(makeAlfrescoBaseurlAdmin() + "/apix/classification/" + aspectQName))
                .header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8)))
                .GET()
                .build();
        HttpResponse<byte[]> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
        byte[] body = response.body();

        assertEquals(new String(body, StandardCharsets.UTF_8), 200, response.statusCode());
        // Same headers as the original Alfresco MVC implementation: no charset parameter, explicit no-cache.
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(null));
        assertEquals(List.of("no-cache"), response.headers().allValues("Cache-Control"));

        return new ObjectMapper().readTree(body);
    }

    private Set<String> serviceRootNodeRefs() {
        Set<String> nodeRefs = new TreeSet<>();
        getBean(ICategoryService.class).getCategoryTree(new QName(GENERAL_CLASSIFIABLE))
                .forEach(category -> nodeRefs.add(category.getNoderef()));
        return nodeRefs;
    }

    private static Set<String> nodeRefs(JsonNode categories) {
        Set<String> nodeRefs = new TreeSet<>();
        categories.forEach(category -> nodeRefs.add(category.get("noderef").asText()));
        return nodeRefs;
    }
}