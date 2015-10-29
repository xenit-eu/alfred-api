package eu.xenit.apix.rest.v2.groups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

class SetUsersInGroupOptions {

    @ApiModelProperty(required = true)
    private String[] users;

    @JsonCreator
    public SetUsersInGroupOptions(@JsonProperty("users") String[] users) {
        this.users = users;
    }

    public String[] getUsers() {
        return users;
    }

}
