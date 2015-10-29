package eu.xenit.apix.alfresco.workflow.alfresco;

import eu.xenit.apix.alfresco.workflow.AbstractApixWorkflowConvertor;
import eu.xenit.apix.alfresco.workflow.WorkflowConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.alfresco.workflow.alfresco.AlfrescoInstanceConverterFactory")
public class AlfrescoInstanceConverterFactory implements WorkflowConverterFactory {

    @Qualifier("eu.xenit.apix.alfresco.workflow.alfresco.AlfrescoProcessInstanceWorkflowConvertor")
    @Autowired
    protected AbstractApixWorkflowConvertor processInstanceWorkflowConvertor;

    @Qualifier("eu.xenit.apix.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor")
    @Autowired
    protected AbstractApixWorkflowConvertor workflowTaskWorkflowConvertor;

    public AbstractApixWorkflowConvertor getProcessInstanceConvertor() {
        return this.processInstanceWorkflowConvertor;
    }

    public AbstractApixWorkflowConvertor getTaskInstanceConvertor() {
        return this.workflowTaskWorkflowConvertor;
    }
}
