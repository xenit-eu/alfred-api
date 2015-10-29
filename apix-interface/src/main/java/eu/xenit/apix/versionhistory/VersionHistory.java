package eu.xenit.apix.versionhistory;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.NodeAssociation;

import java.util.List;

/**
 * Datastructure that represents a list of versions.
 */
public class VersionHistory {

    private List<Version> versionHistory;

    public VersionHistory() {
    }

    public VersionHistory(List<Version> versionHistory) {
        this.versionHistory = versionHistory;
    }

    public List<Version> getVersionHistory() {
        return versionHistory;
    }

    public void setVersionHistory(List<Version> versionHistory) {
        this.versionHistory = versionHistory;
    }
}