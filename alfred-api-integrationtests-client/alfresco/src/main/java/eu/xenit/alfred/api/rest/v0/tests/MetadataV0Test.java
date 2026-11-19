package eu.xenit.alfred.api.rest.v0.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.v1.tests.RestV1BaseTest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The v0 metadata endpoints: GET and POST /apix/eu/xenit/metadata?nodeRef=... and POST /apix/eu/xenit/metadata/bulk.
 */
public class MetadataV0Test extends RestV1BaseTest {

    private static final String CM = "{http://www.alfresco.org/model/content/1.0}";

    private HashMap<String, NodeRef> nodes;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        nodes = init();
    }

    @After
    public void cleanUp() {
        removeMainTestFolder();
    }

    @Test
    public void getReturnsV0Metadata() throws IOException {
        NodeRef file = nodes.get(TESTFILE_NAME);

        HttpResponse response = Request.Get(metadataUrl(file)).execute().returnResponse();

        JsonNode json = assertJsonResponse(response, 200);
        assertEquals(file.getValue(), json.get("id").asText());
        assertEquals(CM + "content", json.get("type").asText());
        assertEquals(TESTFILE_NAME, json.get("properties").get(CM + "name").get(0).get("value").asText());
        assertTrue(json.get("canEditMetadata").asBoolean());
    }

    @Test
    public void postUpdatesMetadataAndDecodesUtf8() throws IOException {
        NodeRef file = nodes.get(TESTFILE_NAME);
        String description = "Alle Empfänger – Україна";

        HttpResponse response = Request.Post(metadataUrl(file))
                .bodyString("{\"propertiesToSet\":{\"cm:description\":[\"" + description + "\"]}}",
                        ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                .execute().returnResponse();

        JsonNode json = assertJsonResponse(response, 200);
        assertEquals(description, json.get("properties").get(CM + "description").get(0).get("value").asText());
    }

    @Test
    public void bulkReturnsMetadataInRequestOrder() throws IOException {
        NodeRef first = nodes.get(TESTFILE2_NAME);
        NodeRef second = nodes.get(TESTFILE_NAME);

        HttpResponse response = Request.Post(baseUrl() + "/bulk")
                .bodyString("[\"" + first.getValue() + "\",\"" + second.getValue() + "\"]",
                        ContentType.APPLICATION_JSON)
                .execute().returnResponse();

        JsonNode json = assertJsonResponse(response, 200);
        assertEquals(2, json.size());
        assertEquals(first.getValue(), json.get(0).get("id").asText());
        assertEquals(second.getValue(), json.get(1).get("id").asText());
    }

    @Test
    public void requestBindingErrorsKeepTheirStatusCodes() throws IOException {
        // Missing required nodeRef parameter.
        assertStatus(400, Request.Get(baseUrl()));
        assertStatus(400, Request.Post(baseUrl()).bodyString("{}", ContentType.APPLICATION_JSON));
        // Unreadable JSON body.
        assertStatus(400, Request.Post(metadataUrl(nodes.get(TESTFILE_NAME)))
                .bodyString("{", ContentType.APPLICATION_JSON));
        // Body that is not declared as JSON.
        assertStatus(415, Request.Post(metadataUrl(nodes.get(TESTFILE_NAME)))
                .bodyString("{}", ContentType.TEXT_PLAIN));
        assertStatus(415, Request.Post(baseUrl() + "/bulk")
                .bodyString("[]", ContentType.TEXT_PLAIN));
    }

    private String baseUrl() {
        return makeAlfrescoBaseurlAdmin() + "/apix/eu/xenit/metadata";
    }

    private String metadataUrl(NodeRef nodeRef) {
        return baseUrl() + "?nodeRef=" + URLEncoder.encode(nodeRef.getValue(), StandardCharsets.UTF_8);
    }

    private static void assertStatus(int expected, Request request) throws IOException {
        assertEquals(expected, request.execute().returnResponse().getStatusLine().getStatusCode());
    }

    private static JsonNode assertJsonResponse(HttpResponse response, int expectedStatus) throws IOException {
        byte[] body = EntityUtils.toByteArray(response.getEntity());
        assertEquals("Unexpected status, response body: " + new String(body, StandardCharsets.UTF_8),
                expectedStatus, response.getStatusLine().getStatusCode());
        assertEquals("application/json", headerValue(response, "Content-Type"));
        assertEquals("no-cache", headerValue(response, "Cache-Control"));
        return new ObjectMapper().readTree(body);
    }

    private static String headerValue(HttpResponse response, String name) {
        Header header = response.getFirstHeader(name);
        assertNotNull("Response has no " + name + " header", header);
        return header.getValue();
    }
}
