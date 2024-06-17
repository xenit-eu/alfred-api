package eu.xenit.apix.rest.jackson;

import eu.xenit.apix.data.NodeRef;

public class Jackson2ApixNodeRefSerializer extends Jackson2ApixAbstractSerializer<NodeRef> {

	private static final long serialVersionUID = 1L;

	public Jackson2ApixNodeRefSerializer() {
		super(NodeRef.class);
	}
}
