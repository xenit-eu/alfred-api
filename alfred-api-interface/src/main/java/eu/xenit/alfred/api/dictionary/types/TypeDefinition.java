package eu.xenit.alfred.api.dictionary.types;

import eu.xenit.alfred.api.data.QName;
import java.util.List;

/**
 * The information of a type: The qname, which is its ID. The qname of the parent, which is the ID of the parent. The
 * title The description
 */
public class TypeDefinition {

    private QName name;
    private QName parent;
    private String title;
    private String description;
    private List<QName> properties;
    private List<QName> mandatoryAspects;

    public TypeDefinition() {
    }

    public List<QName> getProperties() {
        return properties;
    }

    public void setProperties(List<QName> properties) {
        this.properties = properties;
    }

    /**
     * @return The qname of the type.
     */
    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @return The qname of the parent of the type.
     */
    public QName getParent() {
        return parent;
    }

    public void setParent(QName parent) {
        this.parent = parent;
    }

    /**
     * @return The title of the type.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The description of the type.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QName> getMandatoryAspects() {
        return mandatoryAspects;
    }

    public void setMandatoryAspects(List<QName> mandatoryAspects) {
        this.mandatoryAspects = mandatoryAspects;
    }
}
