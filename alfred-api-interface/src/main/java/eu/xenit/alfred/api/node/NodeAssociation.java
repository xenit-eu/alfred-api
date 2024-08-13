package eu.xenit.alfred.api.node;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;

/**
 * Datastructure that represents an association between nodes. An association is between a source and a target. An
 * association has a specific type.
 */
public class NodeAssociation {

    protected NodeRef source;
    protected NodeRef target;
    protected QName type;

    public NodeAssociation() {
    }

    public NodeAssociation(NodeRef source, NodeRef target, QName type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public NodeRef getSource() {
        return source;
    }

    public NodeRef getTarget() {
        return target;
    }

    public QName getType() {
        return type;
    }
}
