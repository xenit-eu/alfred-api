package eu.xenit.alfred.api.node;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;

/**
 * Represents a child parent relation between a source and a target node of a specific type. A child can have multiple
 * parents but should only have one primary parent.
 */
public class ChildParentAssociation extends NodeAssociation {

    private boolean isPrimary;

    public ChildParentAssociation() {
    }

    public ChildParentAssociation(NodeRef source, NodeRef target, QName type, boolean isPrimary) {
        super(source, target, type);
        this.isPrimary = isPrimary;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
}
