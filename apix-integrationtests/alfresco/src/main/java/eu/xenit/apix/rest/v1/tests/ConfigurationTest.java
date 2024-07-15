package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Created by kenneth on 14.03.16.
 */
public class ConfigurationTest extends RestV1BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);

    private INodeService nodeService;
    private IContentService contentService;
    private IFileFolderService apixFileFolderService;
    private NodeArchiveService nodeArchiveService;
    private PermissionService permissionService;

    private NodeRef jsonNodeRef;
    private NodeRef yamlNodeRef;
    private NodeRef otherNodeRef;
    private NodeRef yamlsubNodeRef;

    public ConfigurationTest(){
        // initialise the local beans
        permissionService = serviceRegistry.getPermissionService();
        nodeArchiveService = (NodeArchiveService) testApplicationContext.getBean(NodeArchiveService.class);
        authenticationService = (AuthenticationService) testApplicationContext.getBean("AuthenticationService",AuthenticationService.class);
        // Apix beans
        apixFileFolderService = (IFileFolderService) testApplicationContext.getBean(IFileFolderService.class);
        contentService = (IContentService) testApplicationContext.getBean(IContentService.class);
        nodeService = (eu.xenit.apix.alfresco.metadata.NodeService) testApplicationContext.getBean(eu.xenit.apix.alfresco.metadata.NodeService.class); // fetches APIX nodeService
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = () -> {
            NodeRef dataDictionary = apixFileFolderService.getDataDictionary();
            NodeRef testFolder = apixFileFolderService
                    .createFolder(dataDictionary, "ConfigurationTests");

            jsonNodeRef = nodeService
                    .createNode(testFolder, "xyz.json", new QName(ContentModel.TYPE_CONTENT.toString()));
            ByteArrayInputStream jsonContent = new ByteArrayInputStream("{\"contents\": \"abc\"}".getBytes());
            contentService.setContent(jsonNodeRef, jsonContent, "abc.json");

            yamlNodeRef = nodeService
                    .createNode(testFolder, "xyz.yaml", new QName(ContentModel.TYPE_CONTENT.toString()));
            ByteArrayInputStream yamlContent = new ByteArrayInputStream("contents: abc".getBytes());
            contentService.setContent(yamlNodeRef, yamlContent, "abc.yaml");

            otherNodeRef = nodeService
                    .createNode(testFolder, "xyz.json.disabled", new QName(ContentModel.TYPE_CONTENT.toString()));
            ByteArrayInputStream otherContent = new ByteArrayInputStream("{\"contents\": \"other\"}".getBytes());
            contentService.setContent(otherNodeRef, otherContent, "abc.json");

            NodeRef subFolder = apixFileFolderService.createFolder(testFolder, "subFolder");

            yamlsubNodeRef = nodeService
                    .createNode(subFolder, "sub.yaml", new QName(ContentModel.TYPE_CONTENT.toString()));
            ByteArrayInputStream yamlSubContent = new ByteArrayInputStream("contents: sub".getBytes());
            contentService.setContent(yamlsubNodeRef, yamlSubContent, "abc.yaml");
            return null;
        };

        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);


    }

    private String makeBasePath() {
        return makeAlfrescoBaseurlAdmin() + "/apix/v1/configuration?searchDirectory=ConfigurationTests";
    }

    @Test
    public void testConfigurationGet() throws IOException, JSONException {
        JSONArray jsonFiles = callConfiguration(makeBasePath());
        assertEquals(4, jsonFiles.length());

        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            String nodeRef = jsonFile.optString("nodeRef");
            assertNotNull(nodeRef);

            if (nodeRef.equals(this.jsonNodeRef)) {
                assertEquals("{\"contents\": \"abc\"}", jsonFile.optString("content"));
            }

            assertEquals(jsonFile.toString(), 2, jsonFile.length());
        }

    }

    @Test
    public void testConfigurationGetJS() throws IOException, JSONException {
        HttpResponse response = Request
                .Get(makeBasePath())
                .addHeader(HttpHeaders.ACCEPT, "application/js")
                .execute()
                .returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/js", response.getFirstHeader(CONTENT_TYPE).getValue());
    }

    @Test
    public void testConfigurationGetFields() throws IOException, JSONException {
        String requestUrl = makeBasePath() + "&fields=nodeRef,path,metadata,parsedContent";
        JSONArray jsonFiles = callConfiguration(requestUrl);
        assertEquals(4, jsonFiles.length());

        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);
            assertEquals(jsonFile.toString(), 4, jsonFile.length());
            String nodeRef = jsonFile.optString("nodeRef");
            assertNotNull(nodeRef);

            assertEquals(ContentModel.TYPE_CONTENT.toString(), jsonFile.getJSONObject("metadata").getString("type"));

            if (nodeRef.equals(this.jsonNodeRef.toString())) {
                assertTrue(jsonFile.get("parsedContent") instanceof JSONObject);
                assertEquals("abc", jsonFile.getJSONObject("parsedContent").getString("contents"));

                assertEquals("xyz.json", jsonFile.getJSONObject("metadata").getJSONObject("properties")
                        .getJSONArray(ContentModel.PROP_NAME.toString()).get(0));

                assertEquals("/app:company_home/app:dictionary/cm:ConfigurationTests/cm:xyz.json",
                        jsonFile.getJSONObject("path").getString("qnamePath"));

            }

            if (nodeRef.equals(this.yamlNodeRef.toString())) {
                assertTrue(jsonFile.get("parsedContent") instanceof JSONObject);

                assertEquals("xyz.yaml", jsonFile.getJSONObject("metadata").getJSONObject("properties")
                        .getJSONArray(ContentModel.PROP_NAME.toString()).get(0));
                assertEquals("/app:company_home/app:dictionary/cm:ConfigurationTests/cm:xyz.yaml",
                        jsonFile.getJSONObject("path").getString("qnamePath"));
            }

        }

    }

    private JSONArray callConfiguration(String requestUrl) throws IOException {
        HttpResponse response = Request
                .Get(requestUrl)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .execute()
                .returnResponse();

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", response.getFirstHeader(CONTENT_TYPE).getValue());

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));

        JSONArray jsonFiles = jsonObject.getJSONArray("files");

        return jsonFiles;
    }

    @Test
    public void testConfigurationFilterFields() throws IOException, JSONException {
        String requestUrl = makeBasePath() + "&filter.name=" + URLEncoder.encode("\\.yaml$", "UTF-8");
        JSONArray jsonFiles = callConfiguration(requestUrl);
        assertEquals(2, jsonFiles.length());
        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);
            String nodeRef = jsonFile.optString("nodeRef");
            assertNotNull(nodeRef);
            assertTrue(nodeRef.equals(this.yamlNodeRef.toString()) || nodeRef.equals(this.yamlsubNodeRef.toString()));
        }
    }

    @Test
    public void testConfigurationSubdirectory() throws IOException, JSONException {
        String requestUrl = makeBasePath() + "/subFolder";
        JSONArray jsonFiles = callConfiguration(requestUrl);
        assertEquals(1, jsonFiles.length());
        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);
            String nodeRef = jsonFile.optString("nodeRef");
            assertNotNull(nodeRef);
            assertTrue(nodeRef.equals(this.yamlsubNodeRef.toString()));
        }
    }

    @After
    public void cleanUp() {
        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = () -> {
            try {
                NodeRef dataDictionary = apixFileFolderService.getDataDictionary();
                NodeRef testFolder = apixFileFolderService
                        .getChildNodeRef(dataDictionary, "ConfigurationTests");
                removeTestNode(new org.alfresco.service.cmr.repository.NodeRef(testFolder.toString()));
            } catch (RuntimeException ex) {
                logger.debug("Did not need to remove mainTestFolder because it did not exist");
            }
            return null;
        };
        TransactionService transactionService = serviceRegistry.getTransactionService();
        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
    }
}
