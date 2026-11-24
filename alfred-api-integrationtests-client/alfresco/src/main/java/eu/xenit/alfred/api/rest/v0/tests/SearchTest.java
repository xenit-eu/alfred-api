package eu.xenit.alfred.api.rest.v0.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.rest.v1.tests.RestV1BaseTest;
import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.SearchQueryResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests POST /apix/eu/xenit/search (v0). Read-only: searches the content Alfresco bootstraps.
 */
public class SearchTest extends RestV1BaseTest {

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testSearch() throws IOException {
        HttpResponse response = post(json("{ 'limit': 15, 'query': { 'type': 'cm:content' } }"));
        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        assertEquals(body, 200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", response.getFirstHeader("Content-Type").getValue());

        SearchQueryResult result = new ObjectMapper().readValue(body, SearchQueryResult.class);
        assertFalse(result.getNoderefs().isEmpty());
        assertNotEquals(0L, result.getTotalResultCount());
        // v0 always enables facets and prefixes their names with "@".
        assertFalse(result.getFacets().isEmpty());
        for (FacetSearchResult facet : result.getFacets()) {
            assertTrue(facet.getName(), facet.getName().startsWith("@"));
        }
    }

    @Test
    public void testSearchWithoutBodyIs400() throws IOException {
        assertEquals(400, post("").getStatusLine().getStatusCode());
    }

    private HttpResponse post(String json) throws IOException {
        return Request.Post(makeAlfrescoBaseurlAdmin() + "/apix/eu/xenit/search")
                .bodyString(json, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
    }
}