package eu.xenit.alfred.api.alfresco.workflow.activiti;

import eu.xenit.alfred.api.alfresco.workflow.AbstractApixWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiInstanceConverterFactory")
public class ActivitiInstanceConverterFactory implements WorkflowConverterFactory {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiProcessInstanceWorkflowConvertor")
    @Autowired
    protected AbstractApixWorkflowConvertor activitiProcessInstanceWorkflowConvertor;

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiWorkflowTaskWorkflowConvertor")
    @Autowired
    protected AbstractApixWorkflowConvertor activitiWorkflowTaskWorkflowConvertor;

    public AbstractApixWorkflowConvertor getProcessInstanceConvertor() {
        return this.activitiProcessInstanceWorkflowConvertor;
    }

    public AbstractApixWorkflowConvertor getTaskInstanceConvertor() {
        return this.activitiWorkflowTaskWorkflowConvertor;
    }
}
