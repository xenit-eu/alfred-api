package eu.xenit.apix.rest.v1.nodes;

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

    public NodeInfo() {

    }

    public NodeInfo(NodeRef nodeRef,
            NodeMetadata metadata,
            Map<String, PermissionValue> permissions,
            NodeAssociations associations,
            NodePath path) {
        this.noderef = nodeRef;
        this.metadata = metadata;
        this.permissions = permissions;
        this.associations = associations;
        this.path = path;
    }
}
