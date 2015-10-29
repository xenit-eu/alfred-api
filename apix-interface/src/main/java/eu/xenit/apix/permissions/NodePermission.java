package eu.xenit.apix.permissions;

import java.util.Set;

public class NodePermission {

    private boolean inheritFromParent;
    private Set<Access> ownAccessList;
    private Set<Access> inheritedAccessList;

    public NodePermission() {

    }

    public NodePermission(boolean inheritFromParent, Set<Access> ownAccessList, Set<Access> inheritedAccessList) {
        this.ownAccessList = ownAccessList;
        this.inheritedAccessList = inheritedAccessList;
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

    public Set<Access> getInheritedAccessList() {
        return inheritedAccessList;
    }

    public void setInheritedAccessList(Set<Access> inheritedAccessList) {
        this.inheritedAccessList = inheritedAccessList;
    }

    public static class Access {

        private boolean allowed;
        private String authority;
        private String permission;

        public boolean isAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

    }


}
