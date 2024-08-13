package eu.xenit.alfred.api.server;

import static org.junit.Assert.assertNotNull;

import com.github.ruediste.remoteJUnit.client.RemoteTestRunner;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

@RunWith(RemoteTestRunner.class)
public class AlfrescoApplicationContextIT {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoApplicationContextIT.class);
    private ApplicationContext testApplicationContext;
    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;

    @Before
    public void setUp() {
        this.testApplicationContext = Server.getApplicationContext();
        try {
            serviceRegistry = testApplicationContext.getBean(ServiceRegistry.class);
            nodeService = serviceRegistry.getNodeService();

            if (logger.isTraceEnabled()) {
                String[] beanDefinitionNames = testApplicationContext.getBeanDefinitionNames();
                for (String beanName : beanDefinitionNames) {
                    logger.trace("{}", beanName);
                }
            }
        } catch (BeansException e) {
            logger.error(e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Teardown code here
        logger.error("Cleanup after each test method.");
    }

    @Test
    public void testApplicationContextIsNotNull() {
        assertNotNull(testApplicationContext);
    }

    @Test
    public void testNodeServiceIsNotNull() {
        assertNotNull(nodeService);
    }
}
