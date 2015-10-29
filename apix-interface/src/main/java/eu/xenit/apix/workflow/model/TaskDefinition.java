package eu.xenit.apix.workflow.model;

import java.io.Serializable;

public class TaskDefinition implements Serializable {

    public String id;
    public Object properties;

    public TaskDefinition(String id, Object properties) {
        this.id = id;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }
}
