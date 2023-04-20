package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class ChangeAclsOptions {

    private boolean inheritFromParent;
    private Set<Access> ownAccessList;

    public ChangeAclsOptions( boolean inheritFromParent,
                              Set<Access> ownAccessList) {
        this.ownAccessList = ownAccessList;
        this.inheritFromParent = inheritFromParent;
    }

    public boolean isInheritFromParent() {
        return inheritFromParent;
    }

    public void setInheritFromParent(boolean inheritFromParent) {
        this.inheritFromParent = inheritFromParent;
    }

    public Set<Access> getOwnAccessList() {
        return ownAccessList;
    }

    public void setOwnAccessList(Set<Access> ownAccessList) {
        this.ownAccessList = ownAccessList;
    }

    public static class Access {

        @JsonProperty("allowed")
        public boolean allowed;
        @JsonProperty("authority")
        public String authority;
        @JsonProperty("permission")
        public String permission;
    }
}
