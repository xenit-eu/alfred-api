package eu.xenit.alfred.api.alfresco.workflow.activiti.query;

import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiQueryConverter;
import eu.xenit.alfred.api.alfresco.workflow.AbstractQueryConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiQueryConverterFactory")
public class ActivitiQueryConverterFactory implements AbstractQueryConverterFactory {

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.AlfredApiActivitiWorkflowProcessQueryConverter")
    @Autowired
    protected AbstractAlfredApiQueryConverter alfredApiActivitiWorkflowProcessQueryConverter;

    @Qualifier("eu.xenit.alfred.api.alfresco.workflow.activiti.AlfredApiActivitiWorkflowTaskQueryConverter")
    @Autowired
    protected AbstractAlfredApiQueryConverter alfredApiActivitiWorkflowTaskQueryConverter;

    public AbstractAlfredApiQueryConverter getProcessQueryConverter() {
        return this.alfredApiActivitiWorkflowProcessQueryConverter;
    }

    public AbstractAlfredApiQueryConverter getTasksQueryConverter() {
        return this.alfredApiActivitiWorkflowTaskQueryConverter;
    }
}
