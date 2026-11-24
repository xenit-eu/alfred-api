package eu.xenit.alfred.api.rest.v0.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.rest.v1.tests.RestV1BaseTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests GET /apix/dictionary/checksum (v0, no version in the URL). Read-only.
 */
public class DictionaryChecksumTest extends RestV1BaseTest {

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetDictionaryChecksum() throws IOException {
        HttpResponse response = Request.Get(makeAlfrescoBaseurlAdmin() + "/apix/dictionary/checksum")
                .execute()
                .returnResponse();
        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        assertEquals(body, 200, response.getStatusLine().getStatusCode());
        // Same headers as the original Alfresco MVC implementation: no charset parameter, explicit no-cache.
        assertEquals("application/json", response.getFirstHeader("Content-Type").getValue());
        Header[] cacheControl = response.getHeaders("Cache-Control");
        assertEquals(1, cacheControl.length);
        assertEquals("no-cache", cacheControl[0].getValue());

        JsonNode root = new ObjectMapper().readTree(body);
        assertEquals("Response should only contain 'checksum': " + body, 1, root.size());
        assertTrue(root.get("checksum").isIntegralNumber());
        assertEquals(getBean(IDictionaryService.class).getContentModelCheckSum(), root.get("checksum").asLong());
    }
}