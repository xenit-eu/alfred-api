package eu.xenit.apix.node;

import eu.xenit.apix.data.QName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Datastructure that represents changes to the metadata of a noderef
 */
public class MetadataChanges implements Serializable {

    private QName type;
    private boolean cleanUpAspectsOnGeneralization;
    private QName[] aspectsToAdd;
    private QName[] aspectsToRemove;
    //private Map<String,String[]> propertiesToAdd; //: { [k: string]: string[]; };
    //private String[] propertiesToRemove;
    private Map<QName, String[]> propertiesToSet;

    public MetadataChanges() {

    }

    public MetadataChanges(QName type, QName[] aspectsToAdd, QName[] aspectsToRemove,
            Map<String, String[]> propertiesToAdd, String[] propertiesToRemove,
            Map<QName, String[]> propertiesToSet) {
        this.type = type;
        this.aspectsToAdd = aspectsToAdd;
        this.aspectsToRemove = aspectsToRemove;
        this.propertiesToSet = propertiesToSet;
    }

    public MetadataChanges(QName type, QName[] aspectsToAdd, QName[] aspectsToRemove,
            Map<QName, String[]> propertiesToSet) {
        this(type, false, aspectsToAdd, aspectsToRemove, propertiesToSet);
    }

    public MetadataChanges(QName type, boolean cleanUpAspectsOnGeneralization, QName[] aspectsToAdd,
            QName[] aspectsToRemove,
            Map<QName, String[]> propertiesToSet) {
        this.type = type;
        this.cleanUpAspectsOnGeneralization = cleanUpAspectsOnGeneralization;
        this.aspectsToAdd = aspectsToAdd;
        this.aspectsToRemove = aspectsToRemove;
        this.propertiesToSet = propertiesToSet;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public boolean hasCleanUpAspectsOnGeneralization() {
        return cleanUpAspectsOnGeneralization;
    }

    public void setCleanUpAspectsOnGeneralization(boolean cleanUpAspectsOnGeneralization) {
        this.cleanUpAspectsOnGeneralization = cleanUpAspectsOnGeneralization;
    }

    public QName[] getAspectsToAdd() {
        return aspectsToAdd;
    }

    public void setAspectsToAdd(QName[] aspectsToAdd) {
        this.aspectsToAdd = aspectsToAdd;
    }

    public QName[] getAspectsToRemove() {
        return aspectsToRemove;
    }

    public void setAspectsToRemove(QName[] aspectsToRemove) {
        this.aspectsToRemove = aspectsToRemove;
    }

    public Map<QName, String[]> getPropertiesToSet() {
        return propertiesToSet;
    }

    public void setPropertiesToSet(Map<QName, String[]> propertiesToSet) {
        this.propertiesToSet = propertiesToSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetadataChanges m = ((MetadataChanges) o);

        if(!Objects.equals(m.getType(), type)) {
            return false;
        }
        if(!Objects.equals(m.getAspectsToAdd().length, aspectsToAdd.length)) {
            return false;
        }
        if(!Objects.equals(m.getAspectsToRemove().length, aspectsToRemove.length)) {
            return false;
        }
        if(!Objects.equals(m.getPropertiesToSet().size(), propertiesToSet.size())) {
            return false;
        }

        if(!Arrays.equals(m.getAspectsToAdd(), aspectsToAdd)) {
            return false;
        }

        if(!Arrays.equals(m.getAspectsToRemove(), aspectsToRemove)) {
            return false;
        }

        for(QName q : propertiesToSet.keySet()) {
            if(!m.getPropertiesToSet().containsKey(q)) {
                return false;
            }
            if(!Arrays.equals(m.getPropertiesToSet().get(q), propertiesToSet.get(q))) {
                return false;
            }
        }

        return true;
    }
}
