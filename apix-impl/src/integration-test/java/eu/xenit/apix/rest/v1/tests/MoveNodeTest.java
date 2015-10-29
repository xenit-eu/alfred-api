package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import java.io.IOException;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
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
public class MoveNodeTest extends BaseTest {

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
        final NodeRef[] nodeRef = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(nodeRef[0]);
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
                        assertEquals(2, childAssociationsMainTestFolder.size());
                        assertEquals(1, childAssociationsTestFolder.size());

                        return null;
                    }
                }, false, true);

        final String url = this.makeNodesUrl(nodeRef[0], "/parent", "admin", "admin");
        logger.info(" URL: " + url);

        doPut(url, null, "{\"parent\":\"%s\"}", mainTestFolder.toString());

        List<ChildParentAssociation> newChildAssocsMainTestFolder = nodeService.getChildAssociations(mainTestFolder);
        assertEquals(3, newChildAssocsMainTestFolder.size());
        List<ChildParentAssociation> newChildAssocsTestFolder = nodeService.getChildAssociations(testFolder);
        assertEquals(0, newChildAssocsTestFolder.size());
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
