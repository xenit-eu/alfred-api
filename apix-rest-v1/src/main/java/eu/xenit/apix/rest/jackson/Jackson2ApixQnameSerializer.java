package eu.xenit.apix.rest.jackson;

import eu.xenit.apix.data.QName;

public class Jackson2ApixQnameSerializer extends Jackson2ApixAbstractSerializer<QName> {

	private static final long serialVersionUID = 1L;

	public Jackson2ApixQnameSerializer() {
		super(QName.class);
	}
}
