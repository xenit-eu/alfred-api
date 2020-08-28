package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.NodePath;
import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.PermissionValue;

import java.util.Map;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
public class NodeInfo {

    public NodeRef noderef;
    public NodeMetadata metadata;
    public Map<String, PermissionValue> permissions;
    public NodeAssociations associations;
    public NodePath path;

    @JsonCreator
    public NodeInfo(){

    }

    @JsonCreator
    public NodeInfo(@JsonProperty("nodeRef") NodeRef nodeRef,
            @JsonProperty("metadata") NodeMetadata metadata,
            @JsonProperty("permissions") Map<String, PermissionValue> permissions,
            @JsonProperty("associations") NodeAssociations associations,
            @JsonProperty("path") NodePath path) {
        this.noderef = nodeRef;
        this.metadata = metadata;
        this.permissions = permissions;
        this.associations = associations;
        this.path = path;
    }

    public NodeRef getNoderef() {
        return noderef;
    }

    public void setNoderef(NodeRef noderef) {
        this.noderef = noderef;
    }

    public NodeMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(NodeMetadata metadata) {
        this.metadata = metadata;
    }


    public Map<String, PermissionValue> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, PermissionValue> permissions) {
        this.permissions = permissions;
    }

    public NodeAssociations getAssociations() {
        return associations;
    }

    public void setAssociations(NodeAssociations associations) {
        this.associations = associations;
    }

    public NodePath getPath() {
        return path;
    }

    public void setPath(NodePath path) {
        this.path = path;
    }
}
