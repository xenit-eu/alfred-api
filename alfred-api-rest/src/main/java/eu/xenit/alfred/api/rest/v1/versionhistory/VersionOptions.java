package eu.xenit.alfred.api.rest.v1.versionhistory;

public class VersionOptions {

    private Boolean initialVersion;
    private Boolean autoVersion;
    private Boolean autoVersionOnUpdateProps;

    public VersionOptions() {
    }

    public VersionOptions(Boolean initialVersion, Boolean autoVersion, Boolean autoVersionOnUpdateProps) {
        this.initialVersion = initialVersion;
        this.autoVersion = autoVersion;
        this.autoVersionOnUpdateProps = autoVersionOnUpdateProps;
    }

    public Boolean getInitialVersion() {
        return initialVersion;
    }

    public void setInitialVersion(Boolean initialVersion) {
        this.initialVersion = initialVersion;
    }

    public Boolean getAutoVersion() {
        return autoVersion;
    }

    public void setAutoVersion(Boolean autoVersion) {
        this.autoVersion = autoVersion;
    }

    public Boolean getAutoVersionOnUpdateProps() {
        return autoVersionOnUpdateProps;
    }

    public void setAutoVersionOnUpdateProps(Boolean autoVersionOnUpdateProps) {
        this.autoVersionOnUpdateProps = autoVersionOnUpdateProps;
    }
}
