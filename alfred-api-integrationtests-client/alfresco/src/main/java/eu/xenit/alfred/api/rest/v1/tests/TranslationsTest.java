package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.translation.ITranslationService;
import java.io.IOException;
import java.util.Locale;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests GET /apix/v1/translations/{locale} and GET /apix/v1/translations/{locale}/checksum. Read-only.
 */
public class TranslationsTest extends RestV1BaseTest {

    private static final String LOCALE = "en";

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetTranslations() throws IOException {
        JsonNode root = getJson(makeAlfrescoBaseurlAdmin() + "/apix/v1/translations/" + LOCALE);

        assertTrue("Response should be a JSON object", root.isObject());
        for (String field : new String[]{"types", "aspects", "association", "properties"}) {
            assertTrue("Response is missing '" + field + "'", root.has(field));
            assertTrue("'" + field + "' should be an array", root.get(field).isArray());
        }
        assertTrue("'types' should not be empty", root.get("types").size() > 0);
        assertTrue("'properties' should not be empty", root.get("properties").size() > 0);
    }

    @Test
    public void testGetTranslationsChecksum() throws IOException {
        JsonNode root = getJson(makeAlfrescoBaseurlAdmin() + "/apix/v1/translations/" + LOCALE + "/checksum");

        assertTrue("Response should be a JSON object", root.isObject());
        assertTrue("Response is missing 'checksum'", root.has("checksum"));
        long expected = getBean(ITranslationService.class).getTranslationsCheckSum(Locale.forLanguageTag(LOCALE));
        assertEquals(expected, root.get("checksum").asLong());
    }

    private static JsonNode getJson(String url) throws IOException {
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        byte[] body = EntityUtils.toByteArray(httpResponse.getEntity());

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        // Same headers as the original Alfresco MVC implementation: no charset parameter, explicit no-cache.
        assertEquals("application/json", httpResponse.getFirstHeader("Content-Type").getValue());
        Header[] cacheControl = httpResponse.getHeaders("Cache-Control");
        assertEquals(1, cacheControl.length);
        assertEquals("no-cache", cacheControl[0].getValue());

        return new ObjectMapper().readTree(body);
    }
}
