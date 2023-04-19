package eu.xenit.apix.rest.v1.nodes;

import eu.xenit.apix.data.QName;
import org.alfresco.model.ContentModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
public class CreateNodeOptions {

    public static final QName PROP_NAME_QNAME = new QName(ContentModel.PROP_NAME.toString());
    private String parent;
    private String name;
    private String type;
    private Map<QName, String[]> properties;
    private QName[] aspectsToAdd;
    private QName[] aspectsToRemove;
    private String copyFrom;

    public CreateNodeOptions() {
        this.properties = new HashMap<>(1);
        this.aspectsToRemove = new QName[0];
        this.aspectsToAdd = new QName[0];
    }

    public CreateNodeOptions(String parent,
                             String name,
                             String type,
                             Map<QName, String[]> properties,
                             QName[] aspectsToAdd,
                             QName[] aspectsToRemove,
                             String copyFrom) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.properties = properties;
        if (this.properties == null) {
            this.properties = new HashMap<>(1);
        }
        if ((name != null && !getProperties().containsKey(PROP_NAME_QNAME))) {
            this.properties.put(PROP_NAME_QNAME, new String[]{name});
        }

        this.aspectsToAdd = Objects.requireNonNullElseGet(aspectsToAdd, () -> new QName[0]);

        this.aspectsToRemove = Objects.requireNonNullElseGet(aspectsToRemove, () -> new QName[0]);

        this.copyFrom = copyFrom;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setProperties(Map<QName, String[]> properties) {
        this.properties = properties;
    }

    public void setAspectsToAdd(QName[] aspectsToAdd) {
        this.aspectsToAdd = aspectsToAdd;
    }

    public void setAspectsToRemove(QName[] aspectsToRemove) {
        this.aspectsToRemove = aspectsToRemove;
    }

    public void setCopyFrom(String copyFrom) {
        this.copyFrom = copyFrom;
    }


    public String getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<QName, String[]> getProperties() {
        return properties;
    }

    public QName[] getAspectsToAdd() {
        return aspectsToAdd;
    }

    public QName[] getAspectsToRemove() {
        return aspectsToRemove;
    }

    public String getCopyFrom() {
        return copyFrom;
    }
}
