package eu.xenit.alfred.api.alfresco.workflow.alfresco;

import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoInstanceConverterFactory")
public class AlfrescoInstanceConverterFactory implements WorkflowConverterFactory {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoProcessInstanceWorkflowConvertor")
    @Autowired
    protected AbstractAlfredApiWorkflowConvertor processInstanceWorkflowConvertor;

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor")
    @Autowired
    protected AbstractAlfredApiWorkflowConvertor workflowTaskWorkflowConvertor;

    public AbstractAlfredApiWorkflowConvertor getProcessInstanceConvertor() {
        return this.processInstanceWorkflowConvertor;
    }

    public AbstractAlfredApiWorkflowConvertor getTaskInstanceConvertor() {
        return this.workflowTaskWorkflowConvertor;
    }
}
