package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class InheritFromParent {

    @ApiModelProperty(required = true)
    public boolean inheritFromParent;

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