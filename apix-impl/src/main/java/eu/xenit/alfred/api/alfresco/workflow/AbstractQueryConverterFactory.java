package eu.xenit.alfred.api.alfresco.workflow;

public interface AbstractQueryConverterFactory {

    AbstractApixQueryConverter getProcessQueryConverter();

    AbstractApixQueryConverter getTasksQueryConverter();
}
