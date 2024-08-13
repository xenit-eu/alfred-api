package eu.xenit.alfred.api.rest.jackson;

import eu.xenit.alfred.api.data.NodeRef;

public class Jackson2AlfredApiNodeRefSerializer extends Jackson2AlfredApiAbstractSerializer<NodeRef> {

    private static final long serialVersionUID = 1L;

    public Jackson2AlfredApiNodeRefSerializer() {
        super(NodeRef.class);
    }
}
