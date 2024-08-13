package eu.xenit.alfred.api.rest.v2.groups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class SetSubgroupOptions {

    private String[] subgroups;

    @JsonCreator
    public SetSubgroupOptions(@JsonProperty("subgroups") String[] subgroups) {
        this.subgroups = subgroups;
    }

    public SetSubgroupOptions() {
    }

    public String[] getSubgroups() {
        return subgroups;
    }
}
