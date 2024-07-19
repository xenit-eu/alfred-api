package eu.xenit.apix.tests.transaction;

import static org.junit.Assert.assertTrue;

import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.tests.JavaApiBaseTest;
import eu.xenit.apix.transaction.ITransactionService;
import java.util.concurrent.Callable;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

public class TransactionServiceTest extends JavaApiBaseTest {

    ITransactionService service;
    IFileFolderService ffservice;
    FileInfo mainTestFolder;
    public TransactionServiceTest(){
        service = getBean(ITransactionService.class);
        ffservice = getBean(IFileFolderService.class);
    }

    @Before
    public void SetupTransactionServiceTest() {
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        this.cleanUp();
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
    }


    @Test
    public void TestTransactionService_NotransactionBaseScenario() {
        final TransactionServiceTest me = this;
        FileInfo testNode = me.createTestNode(mainTestFolder.getNodeRef(), "testnode");
        assertTrue(testNode.getName().equals("testnode"));
        eu.xenit.apix.data.NodeRef cNR = ffservice.getChildNodeRef(c.apix(mainTestFolder.getNodeRef()), "testnode");
        assertTrue(cNR != null);
    }

    @Test
    public void TestTransactionServiceSuccessTransaction() {
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