package eu.xenit.apix.alfresco.workflow.activiti.query;

import eu.xenit.apix.alfresco.workflow.AbstractApixQueryConverter;
import eu.xenit.apix.alfresco.workflow.AbstractQueryConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.alfresco.workflow.activiti.ActivitiQueryConverterFactory")
public class ActivitiQueryConverterFactory implements AbstractQueryConverterFactory {

    @Qualifier("eu.xenit.apix.alfresco.workflow.activiti.ApixActivitiWorkflowProcessQueryConverter")
    @Autowired
    protected AbstractApixQueryConverter apixActivitiWorkflowProcessQueryConverter;

    @Qualifier("eu.xenit.apix.alfresco.workflow.activiti.ApixActivitiWorkflowTaskQueryConverter")
    @Autowired
    protected AbstractApixQueryConverter apixActivitiWorkflowTaskQueryConverter;

    public AbstractApixQueryConverter getProcessQueryConverter() {
        return this.apixActivitiWorkflowProcessQueryConverter;
    }

    public AbstractApixQueryConverter getTasksQueryConverter() {
        return this.apixActivitiWorkflowTaskQueryConverter;
    }
}
