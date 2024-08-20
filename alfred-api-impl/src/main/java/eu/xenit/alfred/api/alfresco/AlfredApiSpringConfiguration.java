package eu.xenit.alfred.api.alfresco;

import eu.xenit.alfred.api.alfresco.workflow.WorkflowServiceApsImpl;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowServiceActivitiImpl;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Spring configuration for components based on properties.
 *
 * Components based on Alfresco version are wired in eu.xenit.alfred.api.alfrescoXX.SpringConfiguration (where XX is version)
 */
@Configuration
public class AlfredApiSpringConfiguration {

    private final Logger logger = LoggerFactory.getLogger(AlfredApiSpringConfiguration.class);

    /**
     * Properties read from this AMP's 'alfresco-global.properties' file, exposed as a bean by Alfresco.
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public String getBpm() {
        String bpm = properties.getProperty("bpm");
        if (bpm == null) {
            logger.debug("No valid bpm property found in alfresco-global-properties, defaulting to embedded activiti");
            bpm = "embedded-activiti";
        }
        return bpm;
    }

    @Bean(name = "eu.xenit.alfred.api.workflow.IWorkflowService")
    @DependsOn("global-properties")
    public IWorkflowService workflowService() {
        IWorkflowService result;
        String bpm = getBpm();
        switch (bpm) {
            case "aps":
                result = new WorkflowServiceApsImpl();
                break;
            case "embedded-activiti":
                result = new WorkflowServiceActivitiImpl();
                break;
            default:
                logger.warn("Bpm option not recognised in alfresco-global-properties, defaulting to embedded "
                      + "activiti");
                result = new WorkflowServiceActivitiImpl();
        }
        logger.info("Configuring BPM: using {}", result.getClass());
        return result;
    }
}
