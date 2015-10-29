package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by kenneth on 14.03.16.
 */
public class ConfigurationTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);

    @Autowired
    @Qualifier("FileFolderService")
    FileFolderService fileFolderService;

    @Autowired
    INodeService nodeService;

    @Autowired
    IContentService contentService;

    @Autowired
    IFileFolderService apixFileFolderService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Autowired
    @Qualifier("AuthenticationService")
    AuthenticationService authenticationService;

    @Autowired
    NodeArchiveService nodeArchiveService;

    @Autowired
    PermissionService permissionService;

    private NodeRef jsonNodeRef;
    private NodeRef yamlNodeRef;
    private NodeRef otherNodeRef;
    private NodeRef yamlsubNodeRef;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        TransactionService transactionService = serviceRegistry.getTransactionService();

        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            public Object execute() throws Exception {
                eu.xenit.apix.data.NodeRef dataDictionary = apixFileFolderService.getDataDictionary();
                eu.xenit.apix.data.NodeRef testFolder = apixFileFolderService
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
            }
        };

        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);


    }

    private String makeBasePath() {
        return makeAlfrescoBaseurlAdmin() + "/apix/v1/configuration?searchDirectory=ConfigurationTests";
    }

    @Test
    public void testConfigurationGet() throws IOException, JSONException {
        String requestUrl = makeBasePath();

        HttpResponse response = Request.Get(requestUrl).execute().returnResponse();

        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));

        JSONArray jsonFiles = jsonObject.getJSONArray("files");

        assertEquals(4, jsonFiles.length());

        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            String nodeRef = jsonFile.optString("nodeRef");
            assertNotNull(nodeRef);

            if (nodeRef.equals(this.jsonNodeRef)) {
                assertEquals("{\"contents\": \"abc\"}", jsonFile.optString("content"));
            }

            assertEquals(2, jsonFile.length());
        }

    }

    @Test
    public void testConfigurationGetFields() throws IOException, JSONException {
        String requestUrl = makeBasePath() + "&fields=nodeRef,path,metadata,parsedContent";

        HttpResponse response = Request.Get(requestUrl).execute().returnResponse();

        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));

        JSONArray jsonFiles = jsonObject.getJSONArray("files");

        assertEquals(4, jsonFiles.length());

        for (int i = 0; i < jsonFiles.length(); i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);
            assertEquals(4, jsonFile.length());
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

    @Test
    public void testConfigurationFilterFields() throws IOException, JSONException {
        String requestUrl = makeBasePath() + "&filter.name=" + URLEncoder.encode("\\.yaml$", "UTF-8");

        HttpResponse response = Request.Get(requestUrl).execute().returnResponse();

        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));

        JSONArray jsonFiles = jsonObject.getJSONArray("files");

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

        HttpResponse response = Request.Get(requestUrl).execute().returnResponse();

        assertEquals(200, response.getStatusLine().getStatusCode());

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));

        JSONArray jsonFiles = jsonObject.getJSONArray("files");

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
        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            public Object execute() throws Exception {
                try {
                    eu.xenit.apix.data.NodeRef dataDictionary = apixFileFolderService.getDataDictionary();
                    eu.xenit.apix.data.NodeRef testFolder = apixFileFolderService
                            .getChildNodeRef(dataDictionary, "ConfigurationTests");
                    removeTestNode(new org.alfresco.service.cmr.repository.NodeRef(testFolder.toString()));
                } catch (RuntimeException ex) {
                    logger.debug("Did not need to remove mainTestFolder because it did not exist");
                    //ex.printStackTrace();
                }
                return null;
            }
        };
        TransactionService transactionService = serviceRegistry.getTransactionService();
        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
    }
}
