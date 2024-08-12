package eu.xenit.alfred.api.rest.jackson;

import eu.xenit.alfred.api.data.QName;

public class Jackson2ApixQnameSerializer extends Jackson2ApixAbstractSerializer<QName> {

    private static final long serialVersionUID = 1L;

    public Jackson2ApixQnameSerializer() {
        super(QName.class);
    }
}
