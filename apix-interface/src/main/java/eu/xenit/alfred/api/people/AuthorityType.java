package eu.xenit.alfred.api.people;

public enum AuthorityType {
    EVERYONE;

    public static AuthorityType FromString(String authorityType) {
        if (authorityType == null) {
            return null;
        }
        if (authorityType.toUpperCase() == "EVERYONE") {
            return EVERYONE;
        }
        return null;
    }

}


