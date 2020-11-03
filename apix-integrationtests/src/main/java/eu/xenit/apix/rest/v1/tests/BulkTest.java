package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

/**
 * Created by kenneth on 18.03.16.
 */
public class BulkTest extends BaseTest {

    public static final String AUTHENTICATION_IN_URL = "alf_ticket=";
    private final static Logger logger = LoggerFactory.getLogger(BulkTest.class);
    @Autowired
    INodeService nodeService;

    NodeService alfrescoNodeService;

    TransactionService transactionService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Before
    public void setup() {
        alfrescoNodeService = serviceRegistry.getNodeService();
        transactionService = serviceRegistry.getTransactionService();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetBulk() throws IOException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());
        NodeRef parentRef = primaryParentAssoc.getTarget();

        String url = makeBulkUrl();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        String firstGetUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        String secondGetUrl = removePrefixAndAuthenticate(makeNodesUrl(parentRef, "/metadata", "admin", "admin"));
        httpPost.setEntity(new StringEntity(json(String
                .format("[{'url':'%s','method':'%s'},{'url':'%s','method':'%s'}]", firstGetUrl, "get", secondGetUrl,
                        "get"))));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            logger.info(" Result bulk metadata get: " + result);
            assertEquals(200, response.getStatusLine().getStatusCode());
            JSONArray jsonArray = new JSONArray(result);
            logger.info(" json object 0: " + jsonArray.get(0));
            logger.info(" json object 1: " + jsonArray.get(1));
            JSONObject jsonObject0 = (JSONObject) jsonArray.get(0);
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(1);
            logger.info(" statusCode object 0: " + jsonObject0.getString("statusCode"));
            logger.info(" statusCode object 1: " + jsonObject1.getString("statusCode"));
            assertEquals("200", jsonObject0.getString("statusCode"));
            assertEquals("200", jsonObject1.getString("statusCode"));
        }
    }

    @Test
    public void testUrlEncode() throws IOException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());
        NodeRef parentRef = primaryParentAssoc.getTarget();

        String url = makeBulkUrl();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        String firstGetUrl = "/properties/%7Bhttp://www.alfresco.org/model/content/1.0%7Dname";
        String secondGetUrl = "/properties/" + ContentModel.PROP_OWNER.toString();
        String thirdGetUrl = "/properties/%7Bhttp%3A%2F%2Fwww.alfresco.org%2Fmodel%2Fcontent%2F1.0%7Dname";

        httpPost.setEntity(new StringEntity(json(String.format(
                "[{'url':'%s','method':'get'},{'url':'%s','method':'get'},{'url':'%s','method':'get'}]",
                firstGetUrl, secondGetUrl, thirdGetUrl))));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            logger.info(" Result bulk metadata get: " + result);
            assertEquals(200, response.getStatusLine().getStatusCode());
            JSONArray jsonArray = new JSONArray(result);
            logger.info(" json object 0: " + jsonArray.get(0));
            logger.info(" json object 1: " + jsonArray.get(1));
            logger.info(" json object 2: " + jsonArray.get(2));
            JSONObject jsonObject0 = (JSONObject) jsonArray.get(0);
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(1);
            JSONObject jsonObject2 = (JSONObject) jsonArray.get(2);
            logger.info(" statusCode object 0: " + jsonObject0.getString("statusCode"));
            logger.info(" statusCode object 1: " + jsonObject1.getString("statusCode"));
            logger.info(" statusCode object 2: " + jsonObject2.getString("statusCode"));
            assertEquals("200", jsonObject0.getString("statusCode"));
            assertEquals("200", jsonObject1.getString("statusCode"));
            assertEquals("200", jsonObject2.getString("statusCode"));
        }
    }

    private String removePrefixAndAuthenticate(String fullUrl) {
        String prefix = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1";
        String ret = fullUrl.replace(prefix, "");
        return ret;
    }

    @Test
    public void testPostBulk() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());
        final NodeRef parentRef = primaryParentAssoc.getTarget();

        final String url = makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        String secondPostUrl = removePrefixAndAuthenticate(makeNodesUrl(parentRef, "/metadata", "admin", "admin"));
        String firstJsonBody = String.format("{'aspectsToAdd':['%s']}", ContentModel.ASPECT_VERSIONABLE.toString());
        String secondJsonBody = String.format("{'aspectsToAdd':['%s']}", ContentModel.ASPECT_VERSIONABLE.toString());
        String jsonString = json(
                String.format("[{'url':'%s','method':'%s','body':%s},{'url':'%s','method':'%s','body':%s}]",
                        firstPostUrl, "post", firstJsonBody,
                        secondPostUrl, "post", secondJsonBody));
        httpPost.setEntity(new StringEntity(jsonString));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.info(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertTrue(alfrescoNodeService
                                    .hasAspect(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(BaseTest.TESTFILE_NAME).getValue()),
                                            ContentModel.ASPECT_VERSIONABLE));
                            assertTrue(alfrescoNodeService
                                    .hasAspect(new org.alfresco.service.cmr.repository.NodeRef(parentRef.getValue()),
                                            ContentModel.ASPECT_VERSIONABLE));
                        }
                        return null;
                    }
                }, false, true);

        firstPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));
        secondPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));

        firstJsonBody = String.format("{'propertiesToSet':{'%s':['newName']}}", ContentModel.PROP_NAME.toString());
        jsonString = json(String.format("[{'url':'%s','method':'%s','body':%s}]",
                firstPostUrl, "post", firstJsonBody));
        final HttpPost httpPost2 = new HttpPost(url);
        httpPost2.setEntity(new StringEntity(jsonString));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost2)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.info(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertEquals("newName", alfrescoNodeService
                                    .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(BaseTest.TESTFILE2_NAME).getValue()),
                                            ContentModel.PROP_NAME));
                        }
                        return null;
                    }
                }, false, true);

        secondJsonBody = String.format("{'propertiesToSet':{'%s':['newName']}}", ContentModel.PROP_NAME.toString());
        jsonString = json(String.format("[{'url':'%s','method':'%s','body':%s}]",
                secondPostUrl, "post", secondJsonBody));
        final HttpPost httpPost3 = new HttpPost(url);
        httpPost3.setEntity(new StringEntity(jsonString));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost3)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.info(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertNotEquals("newName", alfrescoNodeService
                                    .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(BaseTest.TESTFILE3_NAME).getValue()),
                                            ContentModel.PROP_NAME));
                        }
                        return null;
                    }
                }, false, true);

    }

    @Test
    public void testSubError() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final String firstUrlForRename = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        final String secondUrlForError = removePrefixAndAuthenticate(
                makeNodesUrl("c03be21a-ebfc-4486-a98a-0d3cae2c40ca", "/metadata", "admin", "admin"));
        final String thirdUrlForRename = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));
        final String firstPostBody = String
                .format("{'propertiesToSet':{'%s':['testName1']}}", ContentModel.PROP_NAME.toString());
        final String secndPostBody = String
                .format("{'propertiesToSet':{'%s':['testName2']}}", ContentModel.PROP_NAME.toString());
        final String thirdPostBody = String
                .format("{'propertiesToSet':{'%s':['testName3']}}", ContentModel.PROP_NAME.toString());
        final String comboPostBody = String.format("[{'url':'%s', 'method': 'post', 'body':%s}," +
                        "{'url':'%s', 'method': 'get', 'body':%s}," +
                        "{'url':'%s', 'method': 'post', 'body':%s}]",
                firstUrlForRename, firstPostBody,
                secondUrlForError, secndPostBody,
                thirdUrlForRename, thirdPostBody);

        final String url = makeBulkUrl();
        final HttpPost post = new HttpPost(url);

        post.setEntity(new StringEntity(json(comboPostBody)));
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(post);

        assertEquals(200, response.getStatusLine().getStatusCode());

        JsonNode node = new ObjectMapper().readTree(response.getEntity().getContent());
        assertEquals(404, node.get(1).get("statusCode").asInt());
    }

    @Test
    public void testPutBulk() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        NodeRef folderRef = primaryParentAssoc.getTarget();

        parentAssociations = this.nodeService.getParentAssociations(folderRef);
        primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef mainFolderRef = primaryParentAssoc.getTarget();

        List<ChildParentAssociation> childAssocs = this.nodeService.getChildAssociations(mainFolderRef);
        assertEquals(3, childAssocs.size());

        String url = this.makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstPutUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "/parent", "admin", "admin"));
        String firstJsonBody = String.format("{'parent':'%s'}", mainFolderRef);
        String jsonString = json(
                String.format("[{'url':'%s', 'method':'%s', 'body':%s}]", firstPutUrl, "put", firstJsonBody));
        httpPost.setEntity(new StringEntity(jsonString));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            List<ChildParentAssociation> newChildAssocs = nodeService
                                    .getChildAssociations(mainFolderRef);
                            assertEquals(4, newChildAssocs.size());
                        }
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testDelete() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE2_NAME));
        ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef folderRef = primaryParentAssoc.getTarget();

        List<ChildParentAssociation> childAssocs = this.nodeService.getChildAssociations(folderRef);
        assertEquals(2, childAssocs.size());

        String url = this.makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstDeleteUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE2_NAME), "admin", "admin"));
        String jsonString = json(String.format("[{'url':'%s', 'method':'%s'}]", firstDeleteUrl, "delete"));
        httpPost.setEntity(new StringEntity(jsonString));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(folderRef);
                            assertEquals(1, newChildAssocs.size());
                        }
                        return null;
                    }
                }, false, true);
        String secondDeleteUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "?permanently=true", "admin",
                        "admin"));
        jsonString = json(String.format("[{'url':'%s', 'method':'%s', 'body':{}}]", secondDeleteUrl, "delete"));
        final HttpPost httpPost2 = new HttpPost(url);
        httpPost2.setEntity(new StringEntity(jsonString));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost2)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.info(" Result bulk delete post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertFalse(alfrescoNodeService
                                    .exists(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(BaseTest.TESTFILE_NAME).getValue())));
                        }
                        return null;
                    }
                });
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
