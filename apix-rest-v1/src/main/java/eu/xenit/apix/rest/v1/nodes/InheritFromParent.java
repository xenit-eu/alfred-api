package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class InheritFromParent {

    private boolean inheritFromParent;

    @JsonCreator
    public InheritFromParent(@JsonProperty("inheritFromParent") boolean inheritFromParent) {
        this.inheritFromParent = inheritFromParent;
    }

    public boolean isInheritFromParent() {
        return this.inheritFromParent;
    }

    public void setInheritFromParent(boolean inheritFromParent) {
        this.inheritFromParent = inheritFromParent;
    }

}