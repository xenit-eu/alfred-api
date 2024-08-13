package eu.xenit.alfred.api.tests.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import eu.xenit.alfred.api.transaction.ITransactionService;
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

    public TransactionServiceTest() {
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
        assertEquals("testnode", testNode.getName());
        eu.xenit.alfred.api.data.NodeRef cNR = ffservice.getChildNodeRef(c.alfredApi(mainTestFolder.getNodeRef()), "testnode");
        assertNotNull(cNR);
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

        assertEquals("testnode", shouldExist.getName());
        eu.xenit.alfred.api.data.NodeRef cNR = ffservice.getChildNodeRef(c.alfredApi(mainTestFolder.getNodeRef()), "testnode");
        assertNotNull(cNR);
    }
}