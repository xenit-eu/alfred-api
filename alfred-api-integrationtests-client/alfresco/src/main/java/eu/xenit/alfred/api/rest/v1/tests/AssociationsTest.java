package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.alfred.api.data.NodeRef;
import java.io.IOException;
import java.util.HashMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kenneth on 17.03.16.
 */
public class AssociationsTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AssociationsTest.class);

    private final NodeService nodeService;

    public AssociationsTest() {
        // initialise the local beans
        nodeService = serviceRegistry.getNodeService();
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testAssociationsGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/associations", "admin",
                "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testAssociationsGetDenied() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/associations",
                RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        HttpResponse response = Request.Get(url).execute().returnResponse();
        assertEquals(403, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testParentAssociationsGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/associations/parents",
                "admin",
                "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testChildAssociationsGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/associations/children",
                "admin",
                "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testPeerAssociationsGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/associations/targets",
                "admin",
                "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateAssociation() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        final NodeRef nodeRefA = initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME);
        final NodeRef nodeRefB = initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME);
        org.alfresco.service.cmr.repository.NodeRef nodeRefAlfredApi = c.alfresco(nodeRefA);
        logger.debug("nodeRefA {} ,  c.alfresco(nodeRefA)={}", nodeRefA, nodeRefAlfredApi);

        final java.util.List<org.alfresco.service.cmr.repository.AssociationRef> assocs = nodeService.getTargetAssocs(
                nodeRefAlfredApi, RegexQNamePattern.MATCH_ALL);

        assertEquals(0, assocs.size());

        doPost(
                createAlfredApiUrl("/nodes/%s/%s/%s/associations", nodeRefA.getStoreRefProtocol(), nodeRefA.getStoreRefId(),
                        nodeRefA.getGuid()),
                null,
                "{'target':'%s','type':'%s'}",
                nodeRefB, c.alfredApi(ContentModel.ASSOC_ORIGINAL));

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        java.util.List<org.alfresco.service.cmr.repository.AssociationRef> newAssocs = nodeService
                                .getTargetAssocs(c.alfresco(nodeRefA), RegexQNamePattern.MATCH_ALL);

                        assertEquals(1, newAssocs.size());
                        assertEquals(c.alfresco(nodeRefB), newAssocs.get(0).getTargetRef());
                        assertEquals(ContentModel.ASSOC_ORIGINAL, newAssocs.get(0).getTypeQName());
                        return null;
                    }
                }, true, true);


    }

    @Test
    public void testRemoveAssociation() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        final NodeRef nodeRefA = initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME);
        final NodeRef nodeRefB = initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME);

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        nodeService.createAssociation(c.alfresco(nodeRefA), c.alfresco(nodeRefB),
                                ContentModel.ASSOC_ORIGINAL);

                        java.util.List<org.alfresco.service.cmr.repository.AssociationRef> assocs = nodeService
                                .getTargetAssocs(c.alfresco(nodeRefA), RegexQNamePattern.MATCH_ALL);

                        assertEquals(1, assocs.size());
                        return null;
                    }
                }, false, true);

        doDelete(
                createAlfredApiUrl("/nodes/%s/%s/%s/associations?target=%s&type=%s",
                        nodeRefA.getStoreRefProtocol(), nodeRefA.getStoreRefId(), nodeRefA.getGuid(),
                        nodeRefB, c.alfredApi(ContentModel.ASSOC_ORIGINAL))
                , null);

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        java.util.List<org.alfresco.service.cmr.repository.AssociationRef> newAssocs = nodeService
                                .getTargetAssocs(c.alfresco(nodeRefA), RegexQNamePattern.MATCH_ALL);

                        assertEquals(0, newAssocs.size());
                        return null;
                    }
                }, false, true);
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
