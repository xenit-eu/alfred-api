package eu.xenit.apix.rest.v2.groups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class SetSubgroupOptions {

    @ApiModelProperty(required = true)
    private String[] subgroups;

    @JsonCreator
    public SetSubgroupOptions(@JsonProperty("subgroups") String[] subgroups) {
        this.subgroups = subgroups;
    }

    public String[] getSubgroups() {
        return subgroups;
    }

}
