package eu.xenit.alfred.api.properties;

import java.util.Map;

/**
 * Datastructure that represents a constraint of a property. constraintType: The type of constraint. parameters: String
 * to Object map which contains more information related to the constraint.
 */
public class PropertyConstraintDefinition {

    private String constraintType;
    private Map<String, Object> parameters;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(String constraintType) {
        this.constraintType = constraintType;
    }
}
