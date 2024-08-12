package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
import java.util.HashMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
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

/**
 * Created by kenneth on 14.03.16.
 */
public class MetadataTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(MetadataTest.class);

    FileFolderService fileFolderService;
    NodeService nodeService;
    TransactionService transactionService;
    NodeArchiveService nodeArchiveService;

    public MetadataTest() {
        // initialise the local beans
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        transactionService = getBean(TransactionService.class);
        nodeArchiveService = getBean(NodeArchiveService.class);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testMetadataGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/metadata?alf_ticket=" + authenticationService.getCurrentTicket(),
                "admin", "admin");
        logger.debug("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMetadataGetAccessDenied() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/metadata",
                RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
        logger.debug("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMetadataPost() throws IOException, JSONException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin");
        logger.debug(" URL: " + url);

        assertFalse(this.nodeService.hasAspect(new org.alfresco.service.cmr.repository.NodeRef(initializedNodeRefs.get(
                        RestV1BaseTest.TESTFILE_NAME).getValue()),
                ContentModel.ASPECT_VERSIONABLE));
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        String propertiesToSet = json(String.format("'propertiesToSet' : {" +
                "'%s' : ['newTitle']" +
                "}", ContentModel.PROP_TITLE));

        //Adding the cm:versionable aspect as a test
        httppost.setEntity(new StringEntity(json(String
                .format("{'aspectsToAdd':['%s'], %s}", ContentModel.ASPECT_VERSIONABLE, propertiesToSet)),
                ContentType.APPLICATION_JSON));

        final NodeRef testNodeRef;
        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.debug(" Result: " + jsonString + " ");
            assertEquals(200, response.getStatusLine().getStatusCode());

            JSONObject jsonObject = new JSONObject(jsonString);
            String nodeId = jsonObject.getString("id");
            testNodeRef = new NodeRef(nodeId);
        }

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    org.alfresco.service.cmr.repository.NodeRef alfTestRef = new org.alfresco.service.cmr.repository.NodeRef(
                            testNodeRef.toString());
                    assertEquals("newTitle", nodeService.getProperty(alfTestRef, ContentModel.PROP_TITLE));
                    assertEquals(RestV1BaseTest.TESTFILE_NAME,
                            nodeService.getProperty(alfTestRef, ContentModel.PROP_NAME));
                    assertTrue(nodeService.hasAspect(alfTestRef, ContentModel.ASPECT_VERSIONABLE));
                    return null;
                }, false, true);
    }

    @Test
    public void testMetadataPostReturnsAccesDenied() throws IOException, JSONException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/metadata",
                RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
        logger.debug(" URL: " + url);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        String propertiesToSet = json(String.format("'propertiesToSet' : {" +
                "'%s' : ['newTitle']" +
                "}", ContentModel.PROP_TITLE));

        //Adding the cm:versionable aspect as a test
        httppost.setEntity(new StringEntity(json(String
                .format("{'aspectsToAdd':['%s'], %s}", ContentModel.ASPECT_VERSIONABLE, propertiesToSet)),
                ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.debug(" Result: " + jsonString + " ");
            assertEquals(403, response.getStatusLine().getStatusCode());
        }
    }

    private String getUrl(NodeRef nodeRef) {
        final String url = makeNodesUrl(nodeRef, "admin", "admin");
        logger.debug(" URL: " + url);
        return url;
    }

    private HashMap<String, NodeRef> CreateAdminNode() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final boolean nodeExists = checkExists(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME));
        assertTrue(nodeExists);
        return initializedNodeRefs;
    }

    private boolean checkExists(NodeRef ref) {
        return this.nodeService.exists(new org.alfresco.service.cmr.repository.NodeRef(ref.getValue()));
    }

    @Test
    public void testDeletePermanently() throws IOException, InterruptedException {
        final HashMap<String, NodeRef> initializedNodeRefs = CreateAdminNode();
        final String url = getUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)) + "?permanently=true";
        Request.Delete(url).execute().returnResponse();
        // Alfresco Cache is lagging behind...
        Thread.sleep(2000);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertFalse(checkExists(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)));
                    org.alfresco.service.cmr.repository.NodeRef archivedRef = nodeArchiveService.getArchivedNode(
                            new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()));
                    logger.debug(" deleted node: {}", archivedRef);
                    assertNotNull(archivedRef);
                    return null;
                }, true, true);
    }

    @Test
    public void testDeleteToArchive() throws IOException, InterruptedException {
        final HashMap<String, NodeRef> initializedNodeRefs = CreateAdminNode();
        final String url = getUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME));
        Request.Delete(url).execute().returnResponse();
        // Alfresco Cache is lagging behind...
        Thread.sleep(2000);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertFalse(checkExists(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)));
                    org.alfresco.service.cmr.repository.NodeRef archivedRef = nodeArchiveService.getArchivedNode(
                            new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()));
                    assertNotNull(archivedRef);
                    return null;
                }, true, true);
    }

    @Test
    public void testDeletePermanentlyReturnsAccesDenied() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME),
                RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS) + "?permanently=true";
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertEquals(403, Request.Delete(url).execute().returnResponse().getStatusLine().getStatusCode());
                    return null;
                }, true, true);
    }


    @Test
    public void testMetadataShortGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getGuid(), "/metadata", "admin",
                "admin");
        logger.debug("URL: " + url);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        logger.debug(EntityUtils.toString(httpResponse.getEntity()));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
