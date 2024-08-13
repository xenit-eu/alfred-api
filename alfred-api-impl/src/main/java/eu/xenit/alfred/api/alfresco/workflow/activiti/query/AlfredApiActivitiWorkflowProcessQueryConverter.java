package eu.xenit.alfred.api.alfresco.workflow.activiti.query;

import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiQueryConverter;
import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.WorkflowConverterFactory;
import eu.xenit.alfred.api.workflow.model.ITaskOrWorkflow;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.activiti.AlfredApiActivitiWorkflowProcessQueryConverter")
public class AlfredApiActivitiWorkflowProcessQueryConverter extends AbstractAlfredApiQueryConverter {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiInstanceConverterFactory")
    @Autowired
    protected WorkflowConverterFactory abstractQueryConverterFactory;

    private AbstractAlfredApiWorkflowConvertor getConvertor() {
        return this.abstractQueryConverterFactory.getProcessInstanceConvertor();
    }

    public AlfredApiHistoricInstanceQuery convertQuery(SearchQuery searchQuery) {
        AlfredApiHistoricProcessInstanceQuery pq = new AlfredApiHistoricProcessInstanceQuery(this.getServiceRegistry());
        this.ApplySearchQuery(searchQuery, pq);
        return pq;
    }

    protected <T> ITaskOrWorkflow convert(T processInstance) {
        return this.getConvertor().apply(processInstance);
    }

    public ITaskOrWorkflow get(String id) {
        return this.getConvertor().apply(id);
    }

    public void update(String id, WorkflowOrTaskChanges changes) {
        this.getConvertor().update(id, changes);
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

    public void claim(String taskID, String userName) {
        this.getConvertor().claim(taskID, userName);
    }

    public void release(String taskID) {
        this.getConvertor().release(taskID);
    }
}
