package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.xenit.apix.data.NodeRef;

import java.util.HashSet;
import java.util.Set;

public class NodeInfoOptions {
    private NodeInfoFlags nodeInfoFlags;
    private Set<NodeRef> nodeReferences;

    @JsonCreator
    public NodeInfoOptions(@JsonProperty(value = "retrieveMetadata", defaultValue = "true") Boolean retrieveMetadata,
                             @JsonProperty(value = "retrievePath", defaultValue = "true") Boolean retrievePath,
                             @JsonProperty(value = "retrievePermissions", defaultValue = "true") Boolean retrievePermissions,
                             @JsonProperty(value = "retrieveAssocs", defaultValue = "true") Boolean retrieveAssocs,
                             @JsonProperty(value = "retrieveChildAssocs", defaultValue = "true") Boolean retrieveChildAssocs,
                             @JsonProperty(value = "retrieveParentAssocs", defaultValue = "true") Boolean retrieveParentAssocs,
                             @JsonProperty(value = "retrieveTargetAssocs", defaultValue = "true") Boolean retrieveTargetAssocs,
                             @JsonProperty(value = "retrieveSourceAssocs", defaultValue = "true") Boolean retrieveSourceAssocs,
                             @JsonProperty(value = "noderefs", required = true) String[] nodeReferences) {
        this.nodeInfoFlags = new NodeInfoFlags(
            retrieveMetadata,
            retrievePath,
            retrievePermissions,
            retrieveAssocs,
            retrieveChildAssocs,
            retrieveParentAssocs,
            retrieveTargetAssocs,
            retrieveSourceAssocs
        );
        this.nodeReferences = new HashSet<>();
        for (String nodeReference : nodeReferences) {
            this.nodeReferences.add(new NodeRef(nodeReference));
        }
    }

    public NodeInfoFlags getNodeInfoFlags() {
        return nodeInfoFlags;
    }

    public void setNodeInfoFlags(NodeInfoFlags nodeInfoFlags) {
        this.nodeInfoFlags = nodeInfoFlags;
    }

    public Set<NodeRef> getNodeReferences() {
        return nodeReferences;
    }

    public void setNodeReferences(Set<NodeRef> nodeReferences) {
        this.nodeReferences = nodeReferences;
    }
}

