package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.data.NodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by kenneth on 14.03.16.
 */
public class MetadataTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(MetadataTest.class);

    @Autowired
    @Qualifier("FileFolderService")
    FileFolderService fileFolderService;

    @Autowired
    @Qualifier("NodeService")
    NodeService nodeService;

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

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testMetadataGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/metadata?alf_ticket=" + authenticationService.getCurrentTicket(),
                "admin", "admin");
        logger.info("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.info(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMetadataGetAccessDenied() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[3], "/metadata",
                "red", "red");
        logger.info("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.info(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMetadataPost() throws IOException, JSONException {
        final NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/metadata", "admin", "admin");
        logger.info(" URL: " + url);

        assertFalse(this.nodeService.hasAspect(new org.alfresco.service.cmr.repository.NodeRef(nodeRef[0].getValue()),
                ContentModel.ASPECT_VERSIONABLE));
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        String propertiesToSet = json(String.format("'propertiesToSet' : {" +
                "'%s' : ['newTitle']" +
                "}", ContentModel.PROP_TITLE));

        //Adding the cm:versionable aspect as a test
        httppost.setEntity(new StringEntity(json(String
                .format("{'aspectsToAdd':['%s'], %s}", ContentModel.ASPECT_VERSIONABLE.toString(), propertiesToSet))));

        final NodeRef testNodeRef;
        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.info(" Result: " + jsonString + " ");
            assertEquals(200, response.getStatusLine().getStatusCode());

            JSONObject jsonObject = new JSONObject(jsonString);
            String nodeId = jsonObject.getString("id");
            testNodeRef = new NodeRef(nodeId);
        }

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        org.alfresco.service.cmr.repository.NodeRef alfTestRef = new org.alfresco.service.cmr.repository.NodeRef(
                                testNodeRef.toString());
                        assertEquals("newTitle", nodeService.getProperty(alfTestRef, ContentModel.PROP_TITLE));
                        assertEquals("testFile", nodeService.getProperty(alfTestRef, ContentModel.PROP_NAME));
                        assertTrue(nodeService.hasAspect(alfTestRef, ContentModel.ASPECT_VERSIONABLE));
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testMetadataPostReturnsAccesDenied() throws IOException, JSONException {
        final NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[3], "/metadata", "red", "red");
        logger.info(" URL: " + url);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        String propertiesToSet = json(String.format("'propertiesToSet' : {" +
                "'%s' : ['newTitle']" +
                "}", ContentModel.PROP_TITLE));

        //Adding the cm:versionable aspect as a test
        httppost.setEntity(new StringEntity(json(String
                .format("{'aspectsToAdd':['%s'], %s}", ContentModel.ASPECT_VERSIONABLE.toString(), propertiesToSet))));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.info(" Result: " + jsonString + " ");
            assertEquals(403, response.getStatusLine().getStatusCode());
        }
    }

    private String getUrl(NodeRef nodeRef) {
        final String url = makeNodesUrl(nodeRef, "admin", "admin");
        logger.info(" URL: " + url);
        return url;
    }

    private NodeRef[] CreateAdminNode() {
        final NodeRef[] nodeRef = init();
        final boolean nodeExists = checkExists(nodeRef[0]);
        assertTrue(nodeExists);
        return nodeRef;
    }

    private boolean checkExists(NodeRef ref) {
        return this.nodeService.exists(new org.alfresco.service.cmr.repository.NodeRef(ref.getValue()));
    }

    @Test
    public void testDeletePermanently() throws IOException {
        final NodeRef[] nodeRef = CreateAdminNode();
        final String url = getUrl(nodeRef[0]) + "?permanently=true";
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        Request.Delete(url).execute().returnResponse();
                        org.alfresco.service.cmr.repository.NodeRef archivedRef = nodeArchiveService.getArchivedNode(
                                new org.alfresco.service.cmr.repository.NodeRef(nodeRef[0].getValue()));
                        assertFalse(checkExists(nodeRef[0]));
                        logger.debug(" deleted node: " + archivedRef.toString());
                        assertNotNull(archivedRef);
                        return null;
                    }
                }, true, true);
    }

    @Test
    public void testDeleteToArchive() throws IOException {
        final NodeRef[] nodeRef = CreateAdminNode();
        final String url = getUrl(nodeRef[0]);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        Request.Delete(url).execute().returnResponse();
                        assertFalse(checkExists(nodeRef[0]));
                        org.alfresco.service.cmr.repository.NodeRef archivedRef = nodeArchiveService.getArchivedNode(
                                new org.alfresco.service.cmr.repository.NodeRef(nodeRef[0].getValue()));
                        assertNotNull(archivedRef);
                        return null;
                    }
                }, true, true);
    }

    @Test
    public void testDeletePermanentlyReturnsAccesDenied() throws IOException {
        final NodeRef[] nodeRef = init();
        final String url = makeNodesUrl(nodeRef[3], "red", "red") + "?permanently=true";
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertEquals(403, Request.Delete(url).execute().returnResponse().getStatusLine().getStatusCode());
                    return null;
                }, true, true);
    }


    @Test
    public void testMetadataShortGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0].getGuid(), "/metadata", "admin", "admin");
        logger.info("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.info(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
