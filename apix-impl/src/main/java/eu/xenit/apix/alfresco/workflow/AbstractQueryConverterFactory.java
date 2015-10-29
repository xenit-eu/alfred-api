package eu.xenit.apix.alfresco.workflow;

public interface AbstractQueryConverterFactory {

    AbstractApixQueryConverter getProcessQueryConverter();

    AbstractApixQueryConverter getTasksQueryConverter();
}
