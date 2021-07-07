package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DictionaryTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(DictionaryTest.class);

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testNamespacesGet() throws IOException, JSONException {

        String url = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/namespaces";
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        JSONObject namespaces = jsonObject.getJSONObject("namespaces");
        logger.error(namespaces.toString());
        String cmNamespace = "http://www.alfresco.org/model/content/1.0";
        JSONObject cm = namespaces.getJSONObject(cmNamespace);
        JSONArray prefixes = cm.getJSONArray("prefixes");
        assertEquals("cm", prefixes.getString(0));
        String name = cm.getString("URI");
        assertEquals(cmNamespace, name);
    }

    private void executeDictionaryTypeTest(String dictionaryType, String shortName, String longName,
            String mandatoryAspect)
            throws IOException, JSONException {
        String baseUrl = makeAlfrescoBaseurlAdmin() + "/apix/v1/dictionary/" + dictionaryType + "/";

        // Short qname lookup
        HttpResponse httpResponse = Request.Get(baseUrl + shortName).execute().returnResponse();
        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(longName, jsonObject.getString("name"));
        if (mandatoryAspect != null) {
            assertMandatoryAspects(jsonObject, mandatoryAspect);
        }

        // full qname lookup
        httpResponse = Request.Get(baseUrl + URLEncoder.encode(longName, "utf-8").replaceAll("%2F", "/")).execute()
                .returnResponse();
        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(longName, jsonObject.getString("name"));

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

        HttpResponse httpResponse = Request.Get(baseUrl).execute().returnResponse();
        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        JSONArray jsonTypes = jsonObject.getJSONArray("types");
        List<String> typeNames = new ArrayList<>(jsonTypes.length());
        for (int i = 0; i < jsonTypes.length(); i++) {
            JSONObject typeDef = jsonTypes.getJSONObject(i);
            typeNames.add(typeDef.getString("name"));
        }
        assertTrue(typeNames.contains("{http://www.alfresco.org/model/system/1.0}base"));
        assertTrue(typeNames.contains("{http://www.alfresco.org/model/content/1.0}content"));

        httpResponse = Request.Get(baseUrl + "?parent=cm:content").execute().returnResponse();
        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        jsonTypes = jsonObject.getJSONArray("types");
        typeNames = new ArrayList<>(jsonTypes.length());
        for (int i = 0; i < jsonTypes.length(); i++) {
            JSONObject typeDef = jsonTypes.getJSONObject(i);
            typeNames.add(typeDef.getString("name"));
        }
        assertFalse(typeNames.contains("{http://www.alfresco.org/model/system/1.0}base"));
        assertTrue(typeNames.contains("{http://www.alfresco.org/model/content/1.0}content"));
    }

    @Test
    public void testAspectDefinitionGet() throws IOException, JSONException {
        executeDictionaryTypeTest("aspects", "cm:complianceable",
                "{http://www.alfresco.org/model/content/1.0}complianceable",
                "{http://www.alfresco.org/model/content/1.0}auditable");
    }

    @Autowired
    private DictionaryDAO dictionaryDAO;

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
        HttpResponse httpResponse = Request.Get(baseUrl + "cm:foobar").execute().returnResponse();

        assertEquals(404, httpResponse.getStatusLine().getStatusCode());
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
