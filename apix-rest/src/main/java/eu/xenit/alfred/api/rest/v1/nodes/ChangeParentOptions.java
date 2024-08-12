package eu.xenit.alfred.api.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
class ChangeParentOptions {

    private String parent;


    @JsonCreator
    public ChangeParentOptions(@JsonProperty("parent") String parent) {
        this.parent = parent;
    }

    public ChangeParentOptions() {
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
