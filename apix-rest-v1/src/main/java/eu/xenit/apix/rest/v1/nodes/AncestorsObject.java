package eu.xenit.apix.rest.v1.nodes;

import eu.xenit.apix.data.NodeRef;
import java.util.List;

public class AncestorsObject {

    private NodeRef node;
    private List<NodeRef> ancestors;

    public NodeRef getNode() {
        return node;
    }

    public List<NodeRef> getAncestors() {
        return ancestors;
    }

    public AncestorsObject(NodeRef node, List<NodeRef> ancestors) {
        this.node = node;
        this.ancestors = ancestors;
    }
}
