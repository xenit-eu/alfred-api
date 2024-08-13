package eu.xenit.alfred.api.workflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class WorkflowOrTaskChanges {

    private Map<String, String> propertiesToSet;

    @JsonCreator
    public WorkflowOrTaskChanges(@JsonProperty("propertiesToSet") Map<String, String> propertiesToSet) {
        this.propertiesToSet = propertiesToSet;
    }

    public WorkflowOrTaskChanges() {
    }

    public Map<String, String> getPropertiesToSet() {
        return propertiesToSet;
    }
}
