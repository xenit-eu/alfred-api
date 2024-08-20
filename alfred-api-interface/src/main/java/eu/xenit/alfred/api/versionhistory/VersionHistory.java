package eu.xenit.alfred.api.versionhistory;

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