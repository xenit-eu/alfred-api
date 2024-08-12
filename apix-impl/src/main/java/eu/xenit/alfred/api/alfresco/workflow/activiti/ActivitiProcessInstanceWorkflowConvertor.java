package eu.xenit.alfred.api.alfresco.workflow.activiti;

import eu.xenit.alfred.api.alfresco.workflow.AbstractApixWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowConverterFactory;
import eu.xenit.alfred.api.workflow.model.ITaskOrWorkflow;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import org.activiti.engine.history.HistoricProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiProcessInstanceWorkflowConvertor")
public class ActivitiProcessInstanceWorkflowConvertor extends AbstractApixWorkflowConvertor {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoInstanceConverterFactory")
    @Autowired
    protected WorkflowConverterFactory alfrescoWorkflowConverterFactory;


    private AbstractApixWorkflowConvertor getConvertor() {
        return this.alfrescoWorkflowConverterFactory.getProcessInstanceConvertor();
    }

    public <T> String getId(T process) {
        return "activiti$" + ((HistoricProcessInstance) process).getId();
    }

    public ITaskOrWorkflow apply(String id) {
        return this.getConvertor().apply(id.startsWith("activiti$") ? id : "activiti$" + id);
    }

    public <T> ITaskOrWorkflow apply(T obj) {
        return this.getConvertor().apply(this.getId(obj));
    }

    public void update(String id, WorkflowOrTaskChanges effectiveChanges) {
        this.getConvertor().update(id, effectiveChanges);
    }

    public void generate(int amount, String username) {
        this.getConvertor().generate(amount, username);
    }

    public void end(String taskID, String transitionID) {
        this.getConvertor().end(taskID, transitionID);
    }

    public void claim(String taskID) {
        this.getConvertor().claim(taskID);
    }

    public void claim(String taskID, String username) {
        this.getConvertor().claim(taskID, username);
    }

    public void release(String taskID) {
        this.getConvertor().release(taskID);
    }
}