package eu.xenit.alfred.api.properties;

import java.util.List;

/*
 * Kept it return List<PropertyDefinition>
 * to make it possible to extend the response with additional data
 */

public class Properties {

    private List<PropertyDefinition> properties;

    public Properties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }
}
