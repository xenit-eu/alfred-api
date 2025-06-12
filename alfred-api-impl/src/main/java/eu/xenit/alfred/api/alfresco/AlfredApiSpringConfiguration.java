package eu.xenit.alfred.api.alfresco;

import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiQueryConverter;
import eu.xenit.alfred.api.alfresco.workflow.AbstractQueryConverterFactory;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowServiceActivitiImpl;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowServiceApsImpl;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import java.util.Properties;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Spring configuration for components based on properties.
 *
 * Components based on Alfresco version are wired in eu.xenit.alfred.api.alfrescoXX.SpringConfiguration (where XX is
 * version)
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
    public IWorkflowService workflowService(
            @Qualifier("ServiceRegistry")
            ServiceRegistry serviceRegistry,
            AlfredApiToAlfrescoConversion c,
            @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.AlfredApiActivitiWorkflowProcessQueryConverter")
            AbstractAlfredApiQueryConverter alfredApiWfProcQueryConverter,
            @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.AlfredApiActivitiWorkflowTaskQueryConverter")
            AbstractAlfredApiQueryConverter alfredApiWfTaskQueryConverter,
            IPeopleService peopleService,
            @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiQueryConverterFactory")
            AbstractQueryConverterFactory activitiQueryConverterFactory) {
        IWorkflowService result;
        String bpm = getBpm();
        switch (bpm) {
            case "aps":
                result = new WorkflowServiceApsImpl(getProperties());
                break;
            case "embedded-activiti":
                result = new WorkflowServiceActivitiImpl(serviceRegistry, c, alfredApiWfProcQueryConverter,
                        alfredApiWfTaskQueryConverter, peopleService, activitiQueryConverterFactory);
                break;
            default:
                logger.warn("Bpm option '{}' not recognised in alfresco-global-properties," +
                        " defaulting to embedded activiti", bpm);
                result = new WorkflowServiceActivitiImpl(serviceRegistry, c, alfredApiWfProcQueryConverter,
                        alfredApiWfTaskQueryConverter, peopleService, activitiQueryConverterFactory);
        }
        logger.info("Configuring BPM: using {}", result.getClass());
        return result;
    }
}
