package eu.xenit.apix.workflow.model;

import java.util.List;

/**
 * POJO for JSON serialization for responding to a /workflows/definitions call It really just needs to wrap a JSON array
 * in a JSON object with the key "data"
 */
public class WorkflowDefinitionList {

    public List<WorkflowDefinition> data;

    public WorkflowDefinitionList(List<WorkflowDefinition> data) {
        this.data = data;
    }

}
