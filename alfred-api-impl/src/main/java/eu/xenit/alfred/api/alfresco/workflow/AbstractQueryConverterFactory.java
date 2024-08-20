package eu.xenit.alfred.api.alfresco.workflow;

public interface AbstractQueryConverterFactory {

    AbstractAlfredApiQueryConverter getProcessQueryConverter();

    AbstractAlfredApiQueryConverter getTasksQueryConverter();
}
