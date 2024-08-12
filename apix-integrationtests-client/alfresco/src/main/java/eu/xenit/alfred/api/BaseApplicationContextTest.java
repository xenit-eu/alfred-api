package eu.xenit.alfred.api;

import com.github.ruediste.remoteJUnit.client.RemoteTestRunner;
import eu.xenit.alfred.api.alfresco.ApixToAlfrescoConversion;
import eu.xenit.alfred.api.server.Server;
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

    protected ServiceRegistry serviceRegistry;
    protected ApixToAlfrescoConversion c;
    protected Repository repository;
    protected ApplicationContext testApplicationContext;
    protected AuthenticationService authenticationService;
    protected SysAdminParams sysAdminParams;
    protected TransactionService transactionService;
    protected RetryingTransactionHelper transactionHelper;

    public BaseApplicationContextTest() {
        // initialise the static application-context
        testApplicationContext = Server.getApplicationContext();
        serviceRegistry = getBean(ServiceRegistry.class);
        c = getBean(ApixToAlfrescoConversion.class);
        repository = getBean(Repository.class);
        transactionService = getBean(TransactionService.class);
        transactionHelper = getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        sysAdminParams = serviceRegistry.getSysAdminParams();
        authenticationService = serviceRegistry.getAuthenticationService();
    }

    public <T> T getBean(String beanClassName, Class<T> beanClass) {
        return testApplicationContext.getBean(beanClassName, beanClass);
    }

    public <T> T getBean(Class<T> beanClass) {
        return testApplicationContext.getBean(beanClass);
    }
}
