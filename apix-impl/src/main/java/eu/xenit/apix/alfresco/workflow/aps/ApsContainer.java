package eu.xenit.apix.alfresco.workflow.aps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApsContainer extends ApsFormField{
    protected int numberOfColumns = 2;
    protected Map<String, List<ApsFormField>> fields = new HashMap<String, List<ApsFormField>>();

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    public Map<String, List<ApsFormField>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<ApsFormField>> fields) {
        if (fields != null) {
            this.fields = fields;
        } else {
            this.fields = new HashMap<String, List<ApsFormField>>();
        }
    }
}
