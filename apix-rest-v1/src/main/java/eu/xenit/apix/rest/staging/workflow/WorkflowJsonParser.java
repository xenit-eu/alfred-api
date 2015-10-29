package eu.xenit.apix.rest.staging.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowJsonParser {

    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
