package eu.xenit.alfred.api.alfresco.workflow;

public interface WorkflowConverterFactory {

    AbstractApixWorkflowConvertor getProcessInstanceConvertor();

    AbstractApixWorkflowConvertor getTaskInstanceConvertor();
}
