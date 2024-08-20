package eu.xenit.alfred.api.rest.v0.metadata;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.node.MetadataChanges;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class MetadataChangesV0 {

    //id: NodeRef;
    private String type;
    private String[] aspectsToAdd;
    private String[] aspectsToRemove;
    private Map<String, String[]> propertiesToAdd; //: { [k: string]: string[]; };
    private String[] propertiesToRemove;
    private Map<String, String[]> propertiesToSet;

    public MetadataChangesV0() {

    }

    public MetadataChangesV0(String type, String[] aspectsToAdd, String[] aspectsToRemove,
            Map<String, String[]> propertiesToAdd, String[] propertiesToRemove,
            Map<String, String[]> propertiesToSet) {
        this.type = type;
        this.aspectsToAdd = aspectsToAdd;
        this.aspectsToRemove = aspectsToRemove;
        this.propertiesToAdd = propertiesToAdd;
        this.propertiesToRemove = propertiesToRemove;
        this.propertiesToSet = propertiesToSet;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getAspectsToAdd() {
        return aspectsToAdd;
    }

    public void setAspectsToAdd(String[] aspectsToAdd) {
        this.aspectsToAdd = aspectsToAdd;
    }

    public String[] getAspectsToRemove() {
        return aspectsToRemove;
    }

    public void setAspectsToRemove(String[] aspectsToRemove) {
        this.aspectsToRemove = aspectsToRemove;
    }

    public Map<String, String[]> getPropertiesToAdd() {
        return propertiesToAdd;
    }

    public void setPropertiesToAdd(Map<String, String[]> propertiesToAdd) {
        this.propertiesToAdd = propertiesToAdd;
    }

    public String[] getPropertiesToRemove() {
        return propertiesToRemove;
    }

    public void setPropertiesToRemove(String[] propertiesToRemove) {
        this.propertiesToRemove = propertiesToRemove;
    }

    public Map<String, String[]> getPropertiesToSet() {
        return propertiesToSet;
    }

    public void setPropertiesToSet(Map<String, String[]> propertiesToSet) {
        this.propertiesToSet = propertiesToSet;
    }


    public MetadataChanges ToV1() {
        MetadataChanges ret = new MetadataChanges();
        if (getType() != null) {
            ret.setType(new QName(getType()));
        }
        if (getAspectsToAdd() != null) {
            ret.setAspectsToAdd(new QName[getAspectsToAdd().length]);
            for (int i = 0; i < getAspectsToAdd().length; i++) {
                ret.getAspectsToAdd()[i] = new QName(getAspectsToAdd()[i]);
            }
        }

        if (getAspectsToRemove() != null) {
            ret.setAspectsToRemove(new QName[getAspectsToRemove().length]);
            for (int i = 0; i < getAspectsToAdd().length; i++) {
                ret.getAspectsToAdd()[i] = new QName(getAspectsToRemove()[i]);
            }
        }

        ret.setPropertiesToSet(new HashMap<QName, String[]>());

        if (getPropertiesToAdd() != null) {
            for (Map.Entry<String, String[]> e : getPropertiesToAdd().entrySet()) {
                ret.getPropertiesToSet().put(new QName(e.getKey()), e.getValue());
            }
        }

        if (getPropertiesToSet() != null) {
            for (Map.Entry<String, String[]> e : getPropertiesToSet().entrySet()) {
                ret.getPropertiesToSet().put(new QName(e.getKey()), e.getValue());
            }
        }

        //Can't remove properties anymore!
        /*for (Map.Entry<String, String[]> e : getPropertiesToRemove().length)
            ret.getPropertiesToSet().put(new QName(e.getKey()), e.getValue());*/
        return ret;

    }

}
