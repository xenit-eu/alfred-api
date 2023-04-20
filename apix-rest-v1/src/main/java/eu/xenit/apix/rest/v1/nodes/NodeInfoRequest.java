package eu.xenit.apix.rest.v1.nodes;

import java.util.List;

public class NodeInfoRequest {
    Boolean retrieveMetadata;
    Boolean retrievePath;
    Boolean retrievePermissions;
    Boolean retrieveAssocs;
    Boolean retrieveChildAssocs;
    Boolean retrieveParentAssocs;
    Boolean retrieveTargetAssocs;
    Boolean retrieveSourceAssocs;
    List<String> noderefs;

    NodeInfoRequest() {
    }

    public NodeInfoRequest(Boolean retrieveMetadata,
                           Boolean retrievePath,
                           Boolean retrievePermissions,
                           Boolean retrieveAssocs,
                           Boolean retrieveChildAssocs,
                           Boolean retrieveParentAssocs,
                           Boolean retrieveTargetAssocs,
                           Boolean retrieveSourceAssocs,
                           List<String> noderefs) {
        this.retrieveMetadata = retrieveMetadata;
        this.retrievePath = retrievePath;
        this.retrievePermissions = retrievePermissions;
        this.retrieveAssocs = retrieveAssocs;
        this.retrieveChildAssocs = retrieveChildAssocs;
        this.retrieveParentAssocs = retrieveParentAssocs;
        this.retrieveTargetAssocs = retrieveTargetAssocs;
        this.retrieveSourceAssocs = retrieveSourceAssocs;
        this.noderefs = noderefs;
    }

    public boolean getRetrieveMetadata() {
        return !Boolean.FALSE.equals(retrieveMetadata);

    }

    public void setRetrieveMetadata(Boolean retrieveMetadata) {
        this.retrieveMetadata = retrieveMetadata;
    }

    public boolean getRetrievePath() {
        return !Boolean.FALSE.equals(retrievePath);
    }

    public void setRetrievePath(Boolean retrievePath) {
        this.retrievePath = retrievePath;
    }

    public boolean getRetrievePermissions() {
        return !Boolean.FALSE.equals(retrievePermissions);

    }

    public void setRetrievePermissions(Boolean retrievePermissions) {
        this.retrievePermissions = retrievePermissions;
    }

    public boolean getRetrieveAssocs() {
        return !Boolean.FALSE.equals(retrieveAssocs);

    }

    public void setRetrieveAssocs(Boolean retrieveAssocs) {
        this.retrieveAssocs = retrieveAssocs;
    }

    public boolean getRetrieveChildAssocs() {
        return !Boolean.FALSE.equals(retrieveChildAssocs);

    }

    public void setRetrieveChildAssocs(Boolean retrieveChildAssocs) {
        this.retrieveChildAssocs = retrieveChildAssocs;
    }

    public boolean getRetrieveParentAssocs() {

        return !Boolean.FALSE.equals(retrieveParentAssocs);

    }

    public void setRetrieveParentAssocs(Boolean retrieveParentAssocs) {
        this.retrieveParentAssocs = retrieveParentAssocs;

    }

    public boolean getRetrieveTargetAssocs() {
        return !Boolean.FALSE.equals(retrieveTargetAssocs);

    }

    public void setRetrieveTargetAssocs(Boolean retrieveTargetAssocs) {
        this.retrieveTargetAssocs = retrieveTargetAssocs;
    }

    public boolean getRetrieveSourceAssocs() {
        return !Boolean.FALSE.equals(retrieveSourceAssocs);

    }

    public void setRetrieveSourceAssocs(Boolean retrieveSourceAssocs) {
        this.retrieveSourceAssocs = retrieveSourceAssocs;
    }

    public List<String> getNoderefs() {
        return noderefs;
    }

    public void setNoderefs(List<String> noderefs) {
        this.noderefs = noderefs;
    }
}
