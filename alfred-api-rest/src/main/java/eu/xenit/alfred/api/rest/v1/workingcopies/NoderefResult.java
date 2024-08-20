package eu.xenit.alfred.api.rest.v1.workingcopies;

import eu.xenit.alfred.api.data.NodeRef;

/**
 * Created by Michiel Huygen on 18/05/2016.
 */
public class NoderefResult {

    private NodeRef noderef;

    public NoderefResult(NodeRef noderef) {
        this.noderef = noderef;
    }

    public NoderefResult() {
    }

    public NodeRef getNoderef() {
        return noderef;
    }

    public void setNoderef(NodeRef noderef) {
        this.noderef = noderef;
    }
}
