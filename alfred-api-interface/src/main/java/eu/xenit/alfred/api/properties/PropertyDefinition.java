package eu.xenit.alfred.api.properties;

import eu.xenit.alfred.api.data.QName;

import java.util.List;

/**
 * Datastructure that represents a property. name: The qname of the property. title description defaultValue dataType
 * multiValued: Whether a node with this can have multiple values. mandatory: Whether a node is required to have a value
 * for this property or not. enforced isProtected indexed: Information around the indexing with this property.
 * constraints: List of constraints of this property.
 */

public class PropertyDefinition {

    private QName name;
    private QName container;
    private String title;
    private String description;
    private String defaultValue;
    private QName dataType;
    private boolean multiValued;
    private boolean mandatory;
    private boolean enforced;
    private boolean isProtected;
    private PropertyIndexOptions indexed;
    private List<PropertyConstraintDefinition> constraints;

    public PropertyDefinition() {
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public QName getDataType() {
        return dataType;
    }

    public void setDataType(QName dataType) {
        this.dataType = dataType;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public void setEnforced(boolean enforced) {
        this.enforced = enforced;
    }

    public boolean isIsProtected() {
        return isProtected;
    }

    public void setIsProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    public PropertyIndexOptions getIndexed() {
        return indexed;
    }

    public void setIndexed(PropertyIndexOptions indexed) {
        this.indexed = indexed;
    }

    public List<PropertyConstraintDefinition> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<PropertyConstraintDefinition> constraints) {
        this.constraints = constraints;
    }

    public QName getContainer() {
        return container;
    }

    public void setContainer(QName container) {
        this.container = container;
    }

    public PropertyDefinition(QName name, QName container, String title, String description, String defaultValue,
            QName dataType, boolean multiValued, boolean mandatory, boolean enforced, boolean isProtected,
            PropertyIndexOptions indexed, List<PropertyConstraintDefinition> constraints) {
        this.name = name;
        this.container = container;
        this.title = title;
        this.description = description;
        this.defaultValue = defaultValue;
        this.dataType = dataType;
        this.multiValued = multiValued;
        this.mandatory = mandatory;
        this.enforced = enforced;
        this.isProtected = isProtected;
        this.indexed = indexed;
        this.constraints = constraints;
    }
}
