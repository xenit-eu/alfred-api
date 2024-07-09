package eu.xenit.apix.rest.v1.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.server.ApplicationContextProvider;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.transaction.TransactionService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.HashMap;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VersionHistoryTest extends RestV1BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VersionHistoryTest.class);

    private ApplicationContext testApplicationContext;
    private ServiceRegistry serviceRegistry;
    TransactionService transactionService;
    NodeService nodeService;
    DictionaryService dictionaryService;
    private org.alfresco.service.cmr.version.VersionService alfrizcoVersionHistoryService;
    private ApixToAlfrescoConversion c;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Setup the RestV1BaseTest Beans
        initialiseBeans();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        serviceRegistry = (ServiceRegistry) testApplicationContext.getBean(ServiceRegistry.class);
        transactionService = (TransactionService) testApplicationContext.getBean(TransactionService.class);
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
        alfrizcoVersionHistoryService = serviceRegistry.getVersionService();
        c =  (ApixToAlfrescoConversion) testApplicationContext.getBean(ApixToAlfrescoConversion.class);
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
                    url[0] = createApixUrl("/versionhistory/%s/%s/%s/versions", testNode.getStoreRefProtocol(),
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

        String versionHistoryUrl = createApixUrl("/versionhistory/%s/%s/%s",
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

        String versionHistoryUrl2 = createApixUrl("/versionhistory/%s/%s/%s",
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

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
