package eu.xenit.alfred.api.workflow.search;

import java.util.HashMap;
import java.util.Map;

public class Facet {

    public String property;
    public Map<String, FacetValue> values;

    public Facet() {
        values = new HashMap<>();
    }

    public Facet(String property, Map<String, FacetValue> values) {
        this.property = property;
        this.values = values;
    }

    public void AddValue(String value) {
        if (value == null) {
            throw new Error("Cannot add null as value for facet " + this.property);
        }
        FacetValue fVal = values.get(value);
        if (fVal == null) {
            fVal = new FacetValue(value, 0);
            values.put(value, fVal);
        }
        fVal.amount++;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Map<String, FacetValue> getValues() {
        return values;
    }

    public void setValues(Map<String, FacetValue> values) {
        this.values = values;
    }
}
