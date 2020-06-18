package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by kenneth on 17.03.16.
 */
public class AssociationsTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AssociationsTest.class);

    @Autowired
    @Qualifier("NodeService")
    NodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Autowired
    ApixToAlfrescoConversion c;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testAssociationsGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/associations", "admin", "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testAssociationsGetDenied() throws IOException {
        NodeRef[] initArray = init();
        String url = makeNodesUrl(initArray[3], "/associations", "red", "red");
        HttpResponse response = Request.Get(url).execute().returnResponse();
        assertEquals(403, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testParentAssociationsGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/associations/parents", "admin", "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testChildAssociationsGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/associations/children", "admin", "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testPeerAssociationsGet() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "/associations/targets", "admin", "admin");

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateAssociation() throws IOException {
        NodeRef[] nodes = init();
        final NodeRef nodeRefA = nodes[0];
        final NodeRef nodeRefB = nodes[1];

        final java.util.List<org.alfresco.service.cmr.repository.AssociationRef> assocs = nodeService
                .getTargetAssocs(c.alfresco(nodeRefA), RegexQNamePattern.MATCH_ALL);

        assertEquals(0, assocs.size());

        doPost(
                createApixUrl("/nodes/%s/%s/%s/associations", nodeRefA.getStoreRefProtocol(), nodeRefA.getStoreRefId(),
                        nodeRefA.getGuid()),
                null,
                "{'target':'%s','type':'%s'}",
                nodeRefB, c.apix(ContentModel.ASSOC_ORIGINAL));

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
        NodeRef[] nodes = init();
        final NodeRef nodeRefA = nodes[0];
        final NodeRef nodeRefB = nodes[1];

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
                createApixUrl("/nodes/%s/%s/%s/associations?target=%s&type=%s",
                        nodeRefA.getStoreRefProtocol(), nodeRefA.getStoreRefId(), nodeRefA.getGuid(),
                        nodeRefB, c.apix(ContentModel.ASSOC_ORIGINAL))
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
