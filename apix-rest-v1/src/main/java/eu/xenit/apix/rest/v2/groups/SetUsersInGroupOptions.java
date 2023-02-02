package eu.xenit.apix.rest.v2.groups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class SetUsersInGroupOptions {

    private String[] users;

    @JsonCreator
    public SetUsersInGroupOptions(@JsonProperty("users") String[] users) {
        this.users = users;
    }

    public String[] getUsers() {
        return users;
    }

}
