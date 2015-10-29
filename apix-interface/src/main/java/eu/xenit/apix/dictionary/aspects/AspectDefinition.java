package eu.xenit.apix.dictionary.aspects;

import eu.xenit.apix.data.QName;

import java.util.List;

public class AspectDefinition {

    private QName name;
    private QName parent;
    private String title;
    private String description;
    private List<QName> properties;

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public QName getParent() {
        return parent;
    }

    public void setParent(QName parent) {
        this.parent = parent;
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

    public List<QName> getProperties() {
        return properties;
    }

    public void setProperties(List<QName> properties) {
        this.properties = properties;
    }
}
