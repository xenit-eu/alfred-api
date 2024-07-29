package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(DictionaryTest.class);
    // Credentials
    private final String username = "admin";
    private final String password = "admin";
    private String encodedAuth;
    private final DictionaryDAO dictionaryDAO;

    public DictionaryTest() {
        // initialise the local beans
        dictionaryDAO = getBean(DictionaryDAO.class);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Set up the basic authentication header
        String auth = username + ":" + password;
        encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Test
    public void testNamespacesGet() throws IOException, JSONException, InterruptedException {

        String url = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/namespaces";
        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            // Create the HttpRequest with the GET method
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, httpResponse.statusCode());
            // Parse the response body into a JSONObject
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(httpResponse.body().toString());
            JsonNode namespaces = jsonNode.get("namespaces");
            String cmNamespace = "http://www.alfresco.org/model/content/1.0";
            JsonNode cm = namespaces.get(cmNamespace);
            logger.error("JsonNode cm {}", cm);
            JsonNode prefixes = cm.get("prefixes");
            assertEquals("cm", prefixes.get(0).asText());
            JsonNode name = cm.get("URI");
            assertEquals(cmNamespace, name.asText());
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    private void executeDictionaryTypeTest(String dictionaryType, String shortName, String longName,
            String mandatoryAspect)
            throws IOException, JSONException {
        String baseUrl = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/" + dictionaryType + "/";

        // Short qname lookup
        String uri = baseUrl + URLEncoder.encode(shortName,
                String.valueOf(Charset.defaultCharset())
        );
        HttpResponse<String> httpResponseQnameLookup = null;
        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            // Create the HttpRequest with the GET method
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            // Send the request and get the response
            httpResponseQnameLookup = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            logger.error("{}", e);
        }
        assertEquals(uri, 200, httpResponseQnameLookup.statusCode());
        JSONObject jsonObject = new JSONObject(httpResponseQnameLookup.body());
        assertEquals(longName, jsonObject.getString("name"));
        if (mandatoryAspect != null) {
            assertMandatoryAspects(jsonObject, mandatoryAspect);
        }

        JSONObject jsonObjectFinal = getRequest(baseUrl, longName);
        // Execute the request
        assertNotNull(jsonObjectFinal);
        assertEquals(longName, jsonObjectFinal.getString("name"));

    }

    public JSONObject getRequest(String baseUrl, String longName) {
        HttpResponse<String> response = null;
        try {
            // Full qualified name to be looked up
            String encodedLongName = URI.create(
                    baseUrl + java.net.URLEncoder.encode(longName, StandardCharsets.UTF_8)
                            .replaceAll("%2F", "/")
            ).toString();

            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create the HttpRequest with the GET method
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(encodedLongName))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            // Send the request and get the response
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            JSONObject jsonObjectFinal = new JSONObject(response.body());
            return jsonObjectFinal;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Test
    public void testPropertyDefinitionGet() throws IOException, JSONException {
        executeDictionaryTypeTest("properties", "cm:name", "{http://www.alfresco.org/model/content/1.0}name", null);
    }

    @Test
    public void testTypeDefinitionGet() throws IOException, JSONException {
        executeDictionaryTypeTest("types", "cm:cmobject", "{http://www.alfresco.org/model/content/1.0}cmobject",
                "{http://www.alfresco.org/model/content/1.0}auditable");
    }

    @Test
    public void testTypesGet() throws IOException, JSONException {
        String baseUrl = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/types";
        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            // Create the HttpRequest with the GET method
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, httpResponse.statusCode());
            // Parse the response body into a JSONObject
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(httpResponse.body().toString());
            JsonNode jsonTypes = jsonNode.get("types");
            List<String> typeNames = new ArrayList<>(jsonTypes.size());
            for (int i = 0; i < jsonTypes.size(); i++) {
                JsonNode typeDef = jsonTypes.get(i);
                typeNames.add(typeDef.get("name").asText());
            }
            assertTrue(typeNames.contains("{http://www.alfresco.org/model/system/1.0}base"));
            assertTrue(typeNames.contains("{http://www.alfresco.org/model/content/1.0}content"));

            // Second test
            request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "?parent=cm:content"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse httpResponseContent = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, httpResponseContent.statusCode());
            JsonNode jsonResponseContent = objectMapper.readTree(httpResponseContent.body().toString());
            jsonTypes = jsonResponseContent.get("types");
            typeNames = new ArrayList<>(jsonTypes.size());
            for (int i = 0; i < jsonTypes.size(); i++) {
                JsonNode typeDef = jsonTypes.get(i);
                typeNames.add(typeDef.get("name").asText());
            }
            assertFalse(typeNames.contains("{http://www.alfresco.org/model/system/1.0}base"));
            assertTrue(typeNames.contains("{http://www.alfresco.org/model/content/1.0}content"));
        } catch (JSONException | InterruptedException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    @Test
    public void testAspectDefinitionGet() throws IOException, JSONException {
        executeDictionaryTypeTest("aspects", "cm:complianceable",
                "{http://www.alfresco.org/model/content/1.0}complianceable",
                "{http://www.alfresco.org/model/content/1.0}auditable");
    }

    @Test
    public void testNamespaceWithDot() throws IOException, JSONException {
        // Setup
        M2Model model = M2Model.createModel("life:model");
        model.createNamespace("life.model", "life");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        M2Type type = model.createType("life:document");
        type.setParentName("cm:content");
        type.setTitle("Life Document");

        dictionaryDAO.putModel(model);

        executeDictionaryTypeTest("types", "life:document", "{life.model}document", null);
    }

    @Test
    public void missingDocumentType_status_404() throws IOException {
        String baseUrl = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/types/";
        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            // Create the HttpRequest with the GET method
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "cm:foobar"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(404, httpResponse.statusCode());
        } catch (JSONException e) {
            fail("Failed to parse JSON response: " + e.getMessage());
        } catch (InterruptedException e) {
            fail("Failed to parse HttpRequest: " + e.getMessage());
        }
    }

    public void assertMandatoryAspects(JSONObject jsonObject, String expectedMandatoryAspect) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("mandatoryAspects");
            boolean aspectFound = false;
            for (int arrayIndex = 0; !aspectFound && arrayIndex < jsonArray.length(); arrayIndex++) {
                String element = jsonArray.getString(arrayIndex);
                if (expectedMandatoryAspect.equals(element)) {
                    aspectFound = true;
                }
            }
            assertTrue("Retrieved Definition does not contain expected mandatory aspect.", aspectFound);
        } catch (JSONException jsonException) {
            logger.error("Caught JSONException for DictionaryTest", jsonException);
            fail();
        }

    }

}
