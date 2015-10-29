package eu.xenit.apix.groups;

import eu.xenit.apix.data.NodeRef;

/**
 * Simple data-container class that represents a group in Alfresco nodeRef: the NodeRef that this group represents
 * identifier: the unique identifying name of this group displayName: the changeable display name of this group
 */
public class Group {

    private NodeRef nodeRef;
    private String identifier;
    private String displayName;

    public Group(NodeRef nodeRef, String identifier, String displayName) {
        this.nodeRef = nodeRef;
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
