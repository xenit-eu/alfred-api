package eu.xenit.apix.alfresco.workflow.activiti.query;

import eu.xenit.apix.alfresco.workflow.AbstractApixQueryConverter;
import eu.xenit.apix.alfresco.workflow.AbstractApixWorkflowConvertor;
import eu.xenit.apix.alfresco.workflow.WorkflowConverterFactory;
import eu.xenit.apix.workflow.model.ITaskOrWorkflow;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.apix.workflow.search.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.alfresco.workflow.activiti.ApixActivitiWorkflowTaskQueryConverter")
public class ApixActivitiWorkflowTaskQueryConverter extends AbstractApixQueryConverter {

    @Qualifier("eu.xenit.apix.alfresco.workflow.activiti.ActivitiInstanceConverterFactory")
    @Autowired
    private WorkflowConverterFactory abstractQueryConverterFactory;

    private AbstractApixWorkflowConvertor getConvertor() {
        return this.abstractQueryConverterFactory.getTaskInstanceConvertor();
    }

    public ApixHistoricInstanceQuery convertQuery(SearchQuery searchQuery) {
        ApixHistoricTaskInstanceQuery tq = new ApixHistoricTaskInstanceQuery(this.getServiceRegistry(),
                this.getPeopleService());
        this.ApplySearchQuery(searchQuery, tq);
        return tq;
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
