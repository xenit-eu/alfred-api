package eu.xenit.apix.tests.transaction;

import static org.junit.Assert.*;

import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.transaction.ITransactionService;
import java.util.concurrent.Callable;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionServiceTest extends BaseTest {

    @Autowired
    public Repository repository;
    @Autowired
    ITransactionService service;
    @Autowired
    IFileFolderService ffservice;

    public FileInfo Setup() {
        this.cleanUp();
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        return this.createMainTestFolder(companyHomeNodeRef);
    }

    @Test
    public void TestTransactionService_NotransactionBaseScenario() {
        final FileInfo mainTestFolder = Setup();
        final TransactionServiceTest me = this;
        FileInfo testNode = me.createTestNode(mainTestFolder.getNodeRef(), "testnode");
        assertTrue(testNode.getName().equals("testnode"));
        eu.xenit.apix.data.NodeRef cNR = ffservice.getChildNodeRef(c.apix(mainTestFolder.getNodeRef()), "testnode");
        assertTrue(cNR != null);
    }

    @Test
    public void TestTransactionServiceSuccessTransaction() {
        final FileInfo mainTestFolder = Setup();
        final TransactionServiceTest me = this;
        final FileInfo shouldExist = service.doInTransaction(new Callable<FileInfo>() {
            @Override
            public FileInfo call() throws Exception {
                FileInfo testNode = me.createTestNode(mainTestFolder.getNodeRef(), "testnode");
                return testNode;
            }
        }, false, false);

        assertTrue(shouldExist.getName().equals("testnode"));
        eu.xenit.apix.data.NodeRef cNR = ffservice.getChildNodeRef(c.apix(mainTestFolder.getNodeRef()), "testnode");
        assertTrue(cNR != null);
    }
}