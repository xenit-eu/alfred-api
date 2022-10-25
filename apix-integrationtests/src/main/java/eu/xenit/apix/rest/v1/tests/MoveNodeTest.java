package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
public class MoveNodeTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(MoveNodeTest.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testMoveNode() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(
                RestV1BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssocTestNode = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef testFolder = primaryParentAssocTestNode.getTarget();

        parentAssociations = this.nodeService.getParentAssociations(testFolder);
        final ChildParentAssociation primaryParentAssocTestFolder = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef mainTestFolder = primaryParentAssocTestFolder.getTarget();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {

                        List<ChildParentAssociation> childAssociationsMainTestFolder = nodeService
                                .getChildAssociations(mainTestFolder);
                        List<ChildParentAssociation> childAssociationsTestFolder = nodeService
                                .getChildAssociations(testFolder);
                        assertEquals(3, childAssociationsMainTestFolder.size());
                        assertEquals(1, childAssociationsTestFolder.size());

                        return null;
                    }
                }, false, true);

        final String url = this.makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/parent", "admin", "admin");
        logger.debug(" URL: " + url);

        doPut(url, null, "{\"parent\":\"%s\"}", mainTestFolder.toString());

        List<ChildParentAssociation> newChildAssocsMainTestFolder = nodeService.getChildAssociations(mainTestFolder);
        assertEquals(4, newChildAssocsMainTestFolder.size());
        List<ChildParentAssociation> newChildAssocsTestFolder = nodeService.getChildAssociations(testFolder);
        assertEquals(0, newChildAssocsTestFolder.size());
    }

    @Test
    public void testMoveNodeReturnsAccesDenied() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(
                RestV1BaseTest.NOUSERRIGHTS_FILE_NAME));
        final ChildParentAssociation primaryParentAssocTestNode = parentAssociations.get(0);
        final NodeRef testFolder = primaryParentAssocTestNode.getTarget();

        parentAssociations = this.nodeService.getParentAssociations(testFolder);
        final ChildParentAssociation primaryParentAssocTestFolder = parentAssociations.get(0);
        final NodeRef mainTestFolder = primaryParentAssocTestFolder.getTarget();

        final String url = this.makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/parent", RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
        logger.debug(" URL: " + url);
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new StringEntity(String.format("{\"parent\":\"%s\"}", mainTestFolder.toString()),
                ContentType.APPLICATION_JSON));

        try(CloseableHttpResponse httpResponse = httpClient.execute(httpPut)) {
            assertEquals(403, httpResponse.getStatusLine().getStatusCode());
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
