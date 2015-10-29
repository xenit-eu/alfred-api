package io.swagger.sample;

import java.util.Map;

/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class MetadataChanges {

    //id: NodeRef;
    private String type;
    private String[] aspectsToAdd;
    private String[] aspectsToRemove;
    private Map<String, String[]> propertiesToAdd; //: { [k: string]: string[]; };
    private String[] propertiesToRemove;

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
}
