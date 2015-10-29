package eu.xenit.apix.rest.v1.nodes;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Michiel Huygen on 23/05/2016.
 */
public class CreateAssociationOptions {

    @ApiModelProperty(required = true)
    private NodeRef target;
    @ApiModelProperty("Defaults to cm:content")
    private QName type = new QName("{http://www.alfresco.org/model/content/1.0}content");

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
