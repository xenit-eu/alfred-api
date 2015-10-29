package eu.xenit.apix.alfresco.workflow;

public interface WorkflowConverterFactory {

    AbstractApixWorkflowConvertor getProcessInstanceConvertor();

    AbstractApixWorkflowConvertor getTaskInstanceConvertor();
}
