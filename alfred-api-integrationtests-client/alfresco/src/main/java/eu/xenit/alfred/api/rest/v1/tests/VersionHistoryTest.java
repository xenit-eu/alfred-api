package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.versionhistory.Version;
import eu.xenit.alfred.api.versionhistory.VersionHistory;
import java.io.IOException;
import java.util.HashMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VersionHistoryTest extends RestV1BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VersionHistoryTest.class);

    NodeService nodeService;
    DictionaryService dictionaryService;
    private final org.alfresco.service.cmr.version.VersionService alfrizcoVersionHistoryService;

    public VersionHistoryTest() {
        // initialise the local beans
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
        alfrizcoVersionHistoryService = serviceRegistry.getVersionService();
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    // Holy
    //
    @Test
    public void testGetVersionHistorySimpleNode() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final String[] url = new String[1];
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    NodeRef testNode = initializedNodeRefs.get(RestV1BaseTest.TESTFILE3_NAME);
                    url[0] = createAlfredApiUrl("/versionhistory/%s/%s/%s/versions", testNode.getStoreRefProtocol(),
                            testNode.getStoreRefId(), testNode.getGuid());
                    HashMap versionProperties = new HashMap<>();
                    versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE,
                            org.alfresco.service.cmr.version.VersionType.MAJOR);
                    versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
                    org.alfresco.service.cmr.version.Version version = alfrizcoVersionHistoryService
                            .createVersion(c.alfresco(testNode), versionProperties);
                    logger.debug(" versioning 1 label: " + version.getVersionLabel());
                    versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test456");
                    org.alfresco.service.cmr.version.Version version2 = alfrizcoVersionHistoryService
                            .createVersion(c.alfresco(testNode), versionProperties);
                    logger.debug(" versioning 2 label: " + version2.getVersionLabel());
                    return null;
                }, false, true);

        HttpResponse httpResponse = Request.Get(url[0]).execute().returnResponse();
        HttpEntity entity = httpResponse.getEntity();
        String result = EntityUtils.toString(entity);
        VersionHistory history = new ObjectMapper().readValue(result, VersionHistory.class);
        Assert.assertEquals(2, history.getVersionHistory().size());
        Version mostRecentVersion = history.getVersionHistory().get(0);
        Assert.assertEquals(mostRecentVersion.getDescription(), "Test456");
        Version oldestVersion = history.getVersionHistory().get(1);
        Assert.assertEquals(oldestVersion.getDescription(), "Test123");
    }

    @Test
    public void testSetVersionHistoryWithoutBody() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final VersionService versionService = alfrizcoVersionHistoryService;

        final boolean defaultAutoVersion = dictionaryService.getProperty(ContentModel.PROP_AUTO_VERSION)
                .getDefaultValue().equals("true");
        final boolean defaultAutoVersionProps = dictionaryService.getProperty(ContentModel.PROP_AUTO_VERSION_PROPS)
                .getDefaultValue().equals("true");
        final boolean defaultInitialVersion = dictionaryService.getProperty(ContentModel.PROP_INITIAL_VERSION)
                .getDefaultValue().equals("true");

        String versionHistoryUrl = createAlfredApiUrl("/versionhistory/%s/%s/%s",
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getStoreRefProtocol(),
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getStoreRefId(),
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getGuid());
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertFalse(versionService
                            .isVersioned(new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue())));
                    return null;
                }, true, true);

        HttpResponse response = Request.Put(versionHistoryUrl)
                .execute()
                .returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertTrue(alfrizcoVersionHistoryService
                            .isVersioned(new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue())));
                    assertEquals(defaultAutoVersion, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()),
                                    ContentModel.PROP_AUTO_VERSION));
                    assertEquals(defaultAutoVersionProps, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()),
                                    ContentModel.PROP_AUTO_VERSION_PROPS));
                    assertEquals(defaultInitialVersion, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()),
                                    ContentModel.PROP_INITIAL_VERSION));
                    assertEquals("1.0", nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()),
                                    ContentModel.PROP_VERSION_LABEL));
                    return null;
                }, true, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertFalse(alfrizcoVersionHistoryService
                            .isVersioned(new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue())));
                    return null;
                }, true, true);

        String versionHistoryUrl2 = createAlfredApiUrl("/versionhistory/%s/%s/%s",
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getStoreRefProtocol(),
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getStoreRefId(),
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getGuid());
        String requestBody = "{" +
                "'autoVersion': false," +
                "'autoVersionOnUpdateProps': true," +
                "'initialVersion': false" +
                "}";

        HttpPut httpPut = new HttpPut(versionHistoryUrl2);
        httpPut.setEntity(new StringEntity(json(requestBody), ContentType.APPLICATION_JSON));
        CloseableHttpResponse httpResponse = HttpClients.createDefault()
                .execute(httpPut);
        assertEquals(200, httpResponse
                .getStatusLine()
                .getStatusCode());

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertTrue(alfrizcoVersionHistoryService
                            .isVersioned(new org.alfresco.service.cmr.repository.NodeRef(
                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue())));
                    assertEquals(false, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue()),
                                    ContentModel.PROP_AUTO_VERSION));
                    assertEquals(true, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue()),
                                    ContentModel.PROP_AUTO_VERSION_PROPS));
                    assertEquals(false, nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue()),
                                    ContentModel.PROP_INITIAL_VERSION));
                    assertEquals("1.0", nodeService
                            .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue()),
                                    ContentModel.PROP_VERSION_LABEL));
                    return null;
                }, true, true);
    }

    @Test
    public void testGetVersionHistoryUnversionedNode() throws IOException {
        NodeRef testNode = init().get(RestV1BaseTest.TESTFILE_NAME);

        HttpResponse response = Request.Get(nodeUrl(testNode, "/versions")).execute().returnResponse();

        // The service returns null for an unversioned node, which the endpoint answers with an empty 200.
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(response.getEntity() == null || EntityUtils.toString(response.getEntity()).isEmpty());
    }

    @Test
    public void testGetRootAndHeadVersion() throws IOException {
        NodeRef testNode = init().get(RestV1BaseTest.TESTFILE3_NAME);
        createTwoVersions(testNode);

        Version root = getVersion(nodeUrl(testNode, "/root"));
        assertEquals("Test123", root.getDescription());
        assertEquals("1.0", root.getLabel());

        Version head = getVersion(nodeUrl(testNode, "/head"));
        assertEquals("Test456", head.getDescription());
        assertEquals("2.0", head.getLabel());
    }

    @Test
    public void testDeleteVersion() throws IOException {
        NodeRef testNode = init().get(RestV1BaseTest.TESTFILE3_NAME);
        createTwoVersions(testNode);

        HttpResponse response = Request.Delete(nodeUrl(testNode, "/versions/2.0")).execute().returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());

        VersionHistory history = getVersionHistory(testNode);
        assertEquals(1, history.getVersionHistory().size());
        assertEquals("1.0", history.getVersionHistory().get(0).getLabel());
    }

    @Test
    public void testRevertVersion() throws IOException {
        NodeRef testNode = init().get(RestV1BaseTest.TESTFILE3_NAME);
        // Give each version a different title, so the revert has something observable to restore.
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            HashMap<String, java.io.Serializable> versionProperties = new HashMap<>();
            versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE,
                    org.alfresco.service.cmr.version.VersionType.MAJOR);
            nodeService.setProperty(c.alfresco(testNode), ContentModel.PROP_TITLE, "title of 1.0");
            alfrizcoVersionHistoryService.createVersion(c.alfresco(testNode), versionProperties);
            nodeService.setProperty(c.alfresco(testNode), ContentModel.PROP_TITLE, "title of 2.0");
            alfrizcoVersionHistoryService.createVersion(c.alfresco(testNode), versionProperties);
            return null;
        }, false, true);

        HttpResponse response = Request.Post(nodeUrl(testNode, "/versions/1.0/revert")).execute().returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());

        // Reverting restores the properties of version 1.0 without removing later versions from the history.
        // Alfresco does not reset cm:versionLabel on revert, so the title is what shows the revert happened.
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            assertEquals("title of 1.0", nodeService.getProperty(c.alfresco(testNode), ContentModel.PROP_TITLE));
            return null;
        }, true, true);
        assertEquals(2, getVersionHistory(testNode).getVersionHistory().size());
    }

    @Test
    public void testDeleteVersionHistory() throws IOException {
        NodeRef testNode = init().get(RestV1BaseTest.TESTFILE3_NAME);
        createTwoVersions(testNode);

        HttpResponse response = Request.Delete(nodeUrl(testNode, "")).execute().returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            // The versionable aspect stays on the node; only the history is removed.
            assertTrue(alfrizcoVersionHistoryService.getVersionHistory(c.alfresco(testNode)) == null);
            return null;
        }, true, true);
    }

    private String nodeUrl(NodeRef node, String suffix) {
        return createAlfredApiUrl("/versionhistory/%s/%s/%s" + suffix,
                node.getStoreRefProtocol(), node.getStoreRefId(), node.getGuid());
    }

    /** Creates major versions 1.0 ("Test123") and 2.0 ("Test456"). */
    private void createTwoVersions(NodeRef node) {
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            HashMap<String, java.io.Serializable> versionProperties = new HashMap<>();
            versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE,
                    org.alfresco.service.cmr.version.VersionType.MAJOR);
            versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
            alfrizcoVersionHistoryService.createVersion(c.alfresco(node), versionProperties);
            versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test456");
            alfrizcoVersionHistoryService.createVersion(c.alfresco(node), versionProperties);
            return null;
        }, false, true);
    }

    private VersionHistory getVersionHistory(NodeRef node) throws IOException {
        HttpResponse response = Request.Get(nodeUrl(node, "/versions")).execute().returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());
        return new ObjectMapper().readValue(EntityUtils.toString(response.getEntity()), VersionHistory.class);
    }

    private static Version getVersion(String url) throws IOException {
        HttpResponse response = Request.Get(url).execute().returnResponse();
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", response.getFirstHeader("Content-Type").getValue());
        return new ObjectMapper().readValue(EntityUtils.toString(response.getEntity()), Version.class);
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
