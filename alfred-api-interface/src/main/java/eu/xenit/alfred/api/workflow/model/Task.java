package eu.xenit.alfred.api.workflow.model;

import eu.xenit.alfred.api.utils.SerializableUtils;
import java.io.Serializable;
import java.util.Map;

public class Task implements ITaskOrWorkflow {

    public String id;
    public Map<String, Serializable> properties;

    public Map<String, Serializable> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
    }

    public String GetStringProperty(String property) {
        Serializable value = properties.get(property);
        return SerializableUtils.toString(value);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
