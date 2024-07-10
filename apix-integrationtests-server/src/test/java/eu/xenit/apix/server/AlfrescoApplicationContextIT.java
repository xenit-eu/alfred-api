package eu.xenit.apix.server;

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
        this.testApplicationContext = ApplicationContextProvider.getApplicationContext();

        try{
            serviceRegistry = (ServiceRegistry) testApplicationContext.getBean(ServiceRegistry.class);
            nodeService = serviceRegistry.getNodeService();

//            // Print out all available beans.
//            String[] beanDefinitionNames = testApplicationContext.getBeanDefinitionNames();
//            for(String beanName : beanDefinitionNames){
//                System.out.println(beanName);
//            }
        }catch (BeansException e){
            System.out.println(e.getMessage());
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
