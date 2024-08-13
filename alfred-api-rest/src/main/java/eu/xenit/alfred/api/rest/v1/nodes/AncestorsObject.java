package eu.xenit.alfred.api.rest.v1.nodes;

import eu.xenit.alfred.api.data.NodeRef;
import java.util.List;

public class AncestorsObject {

    private NodeRef node;
    private List<NodeRef> ancestors;

    public AncestorsObject(NodeRef node, List<NodeRef> ancestors) {
        this.node = node;
        this.ancestors = ancestors;
    }

    public AncestorsObject() {
    }

    public NodeRef getNode() {
        return node;
    }

    public void setNode(NodeRef node) {
        this.node = node;
    }

    public List<NodeRef> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<NodeRef> ancestors) {
        this.ancestors = ancestors;
    }
}
