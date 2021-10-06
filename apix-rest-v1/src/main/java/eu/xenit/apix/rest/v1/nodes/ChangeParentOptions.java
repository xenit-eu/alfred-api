package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class ChangeParentOptions {

    @ApiModelProperty(required = true)
    public String parent;

    @JsonCreator
    public ChangeParentOptions(@JsonProperty("parent") String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return parent;
    }
}
