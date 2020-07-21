package eu.xenit.apix.rest.v1.nodes;

public class NodeInfoFlags {
    private Boolean retrieveMetadata;
    private Boolean retrievePath;
    private Boolean retrievePermissions;
    private Boolean retrieveAssocs;
    private Boolean retrieveChildAssocs;
    private Boolean retrieveParentAssocs;
    private Boolean retrieveTargetAssocs;
    private Boolean retrieveSourceAssocs;

    public NodeInfoFlags(Boolean retrieveMetadata,
                         Boolean retrievePath,
                         Boolean retrievePermissions,
                         Boolean retrieveAssocs,
                         Boolean retrieveChildAssocs,
                         Boolean retrieveParentAssocs,
                         Boolean retrieveTargetAssocs,
                         Boolean retrieveSourceAssocs) {
        this.retrieveMetadata = retrieveMetadata != null ? retrieveMetadata : true;
        this.retrievePath = retrievePath != null ? retrievePath : true;
        this.retrievePermissions = retrievePermissions != null ? retrievePermissions : true;
        this.retrieveAssocs = retrieveAssocs != null ? retrieveAssocs : true;
        this.retrieveChildAssocs = retrieveChildAssocs != null ? retrieveChildAssocs : true;
        this.retrieveParentAssocs = retrieveParentAssocs != null ? retrieveParentAssocs : true;
        this.retrieveTargetAssocs = retrieveTargetAssocs != null ? retrieveTargetAssocs : true;
        this.retrieveSourceAssocs = retrieveSourceAssocs != null ? retrieveSourceAssocs : true;

    }

    public static NodeInfoFlags CreateNewNodeInfoFlagsWithAllFlagsTrue() {
        return new NodeInfoFlags(
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
        );
    }

    public Boolean getRetrieveMetadata() {
        return retrieveMetadata;
    }
    public void setRetrieveMetadata(Boolean retrieveMetadata) {
        this.retrieveMetadata = retrieveMetadata;
    }
    public Boolean getRetrievePath() {
        return retrievePath;
    }
    public void setRetrievePath(Boolean retrievePath) {
        this.retrievePath = retrievePath;
    }
    public Boolean getRetrievePermissions() {
        return retrievePermissions;
    }
    public void setRetrievePermissions(Boolean retrievePermissions) {
        this.retrievePermissions = retrievePermissions;
    }
    public Boolean getRetrieveAssocs() {
        return retrieveAssocs;
    }
    public void setRetrieveAssocs(Boolean retrieveAssocs) {
        this.retrieveAssocs = retrieveAssocs;
    }
    public Boolean getRetrieveChildAssocs() {
        return retrieveChildAssocs;
    }
    public void setRetrieveChildAssocs(Boolean retrieveChildAssocs) {
        this.retrieveChildAssocs = retrieveChildAssocs;
    }
    public Boolean getRetrieveParentAssocs() {
        return retrieveParentAssocs;
    }
    public void setRetrieveParentAssocs(Boolean retrieveParentAssocs) {
        this.retrieveParentAssocs = retrieveParentAssocs;
    }
    public Boolean getRetrieveTargetAssocs() {
        return retrieveTargetAssocs;
    }
    public void setRetrieveTargetAssocs(Boolean retrieveTargetAssocs) {
        this.retrieveTargetAssocs = retrieveTargetAssocs;
    }
    public Boolean getRetrieveSourceAssocs() {
        return retrieveSourceAssocs;
    }
    public void setRetrieveSourceAssocs(Boolean retrieveSourceAssocs) {
        this.retrieveSourceAssocs = retrieveSourceAssocs;
    }
}
