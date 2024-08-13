package eu.xenit.alfred.api.alfresco.workflow;

public interface WorkflowConverterFactory {

    AbstractAlfredApiWorkflowConvertor getProcessInstanceConvertor();

    AbstractAlfredApiWorkflowConvertor getTaskInstanceConvertor();
}
