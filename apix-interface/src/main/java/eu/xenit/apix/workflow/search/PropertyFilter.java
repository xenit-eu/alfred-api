package eu.xenit.apix.workflow.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PropertyFilter implements IQueryFilter {

    public static final String TYPE = "PropertyFilter";
    private String property;
    private String value;

    @JsonCreator
    public PropertyFilter(@JsonProperty("value") String value, @JsonProperty("property") String property,
            @JsonProperty("type") String type) {
        this.value = value;
        this.property = property;
    }

    public String getValue() {
        return this.value;
    }

    public String getProperty() {
        return this.property;
    }

    public String getType() {
        return this.TYPE;
    }

    public int getIntValue() {
        return Integer.parseInt(getValue());
    }

    public boolean getBooleanValue() {
        return Boolean.parseBoolean(getValue());
    }
}
