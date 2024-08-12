package eu.xenit.alfred.api.versionhistory;

import eu.xenit.alfred.api.data.NodeRef;

import java.util.Date;

/**
 * Datastructure containing the information of a single version. modifier: The person who made this version.
 * modifiedDate: When the version was created. description: The description of this version. type: Whether the version
 * is a major or a minor version. nodeRef: The noderef of the version itself.
 */
public class Version {

    private String modifier;
    private Date modifiedDate;
    private String label;
    private String description;
    private VersionType type;
    private NodeRef nodeRef;

    public Version() {

    }

    public Version(String modifier, Date modifiedDate, String label, String description, VersionType type,
            NodeRef nodeRef) {
        this.modifier = modifier;
        this.modifiedDate = modifiedDate;
        this.label = label;
        this.description = description;
        this.type = type;
        this.nodeRef = nodeRef;

    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VersionType getType() {
        return type;
    }

    public void setType(VersionType type) {
        this.type = type;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public enum VersionType {
        MAJOR,
        MINOR,
        UNKNOWN
    }
}
