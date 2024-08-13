package eu.xenit.alfred.api.alfresco.workflow.activiti;

import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiInstanceConverterFactory")
public class ActivitiInstanceConverterFactory implements WorkflowConverterFactory {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiProcessInstanceWorkflowConvertor")
    @Autowired
    protected AbstractAlfredApiWorkflowConvertor activitiProcessInstanceWorkflowConvertor;

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiWorkflowTaskWorkflowConvertor")
    @Autowired
    protected AbstractAlfredApiWorkflowConvertor activitiWorkflowTaskWorkflowConvertor;

    public AbstractAlfredApiWorkflowConvertor getProcessInstanceConvertor() {
        return this.activitiProcessInstanceWorkflowConvertor;
    }

    public AbstractAlfredApiWorkflowConvertor getTaskInstanceConvertor() {
        return this.activitiWorkflowTaskWorkflowConvertor;
    }
}
