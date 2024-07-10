package eu.xenit.apix;

import com.github.ruediste.remoteJUnit.client.RemoteTestRunner;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.server.ApplicationContextProvider;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(RemoteTestRunner.class)
public abstract class BaseApplicationContextTest {
    private final static Logger logger = LoggerFactory.getLogger(BaseApplicationContextTest.class);

    protected ApixToAlfrescoConversion c;
    protected ServiceRegistry serviceRegistry;
    protected Repository repository;
    protected ApplicationContext testApplicationContext;
    protected AuthenticationService authenticationService;
    protected SysAdminParams sysAdminParams;
    protected TransactionService transactionService;
    protected RetryingTransactionHelper transactionHelper;

    public BaseApplicationContextTest() {
        // initialise the static application-context
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        serviceRegistry = testApplicationContext.getBean(ServiceRegistry.class);
        c =  testApplicationContext.getBean(ApixToAlfrescoConversion.class);
        repository = testApplicationContext.getBean(Repository.class);
        sysAdminParams = serviceRegistry.getSysAdminParams();
        authenticationService = serviceRegistry.getAuthenticationService();
        transactionService = testApplicationContext.getBean(TransactionService.class);
        transactionHelper = testApplicationContext.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    }
}
