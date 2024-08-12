package eu.xenit.alfred.api.rest.v1.nodes;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;

/**
 * Created by Michiel Huygen on 23/05/2016.
 */
public class CreateAssociationOptions {

    private NodeRef target;
    private QName type = new QName("{http://www.alfresco.org/model/content/1.0}content");

    public CreateAssociationOptions(NodeRef target, QName type) {
        this.target = target;
        this.type = type;
    }

    public CreateAssociationOptions() {
    }

    public NodeRef getTarget() {
        return target;
    }

    public void setTarget(NodeRef target) {
        this.target = target;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }
}
