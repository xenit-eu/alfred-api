package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the deprecated GET /apix/v1/properties/{qname}.
 */
public class PropertiesTest extends RestV1BaseTest {

    private static final String FULL_QNAME = "{http://www.alfresco.org/model/content/1.0}name";

    private String encodedAuth;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        encodedAuth = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testGetPropertyByShortQName() throws IOException, InterruptedException {
        assertNameProperty(get("/apix/v1/properties/cm:name"));
    }

    @Test
    public void testGetPropertyWithQnameWithSlashParameter() throws IOException, InterruptedException {
        // The path segment is ignored when qnameWithSlash is given.
        assertNameProperty(get("/apix/v1/properties/x?qnameWithSlash="
                + URLEncoder.encode(FULL_QNAME, StandardCharsets.UTF_8)));
    }

    @Test
    public void testMissingPropertyIsEmpty404() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/apix/v1/properties/cm:foobar");
        assertEquals(404, response.statusCode());
        assertTrue("404 should have an empty body", response.body().isEmpty());
        assertFalse("404 should have no Content-Type", response.headers().firstValue("Content-Type").isPresent());
        assertEquals("no-cache", response.headers().firstValue("Cache-Control").orElse(null));
    }

    private static void assertNameProperty(HttpResponse<String> response) throws IOException {
        assertEquals(response.body(), 200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(null));
        assertEquals("no-cache", response.headers().firstValue("Cache-Control").orElse(null));
        assertEquals(FULL_QNAME, new ObjectMapper().readTree(response.body()).get("name").asText());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(makeAlfrescoBaseurlAdmin() + path))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}