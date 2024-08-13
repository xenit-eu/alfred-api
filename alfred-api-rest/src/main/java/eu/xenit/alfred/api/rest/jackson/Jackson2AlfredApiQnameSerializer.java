package eu.xenit.alfred.api.rest.jackson;

import eu.xenit.alfred.api.data.QName;

public class Jackson2AlfredApiQnameSerializer extends Jackson2AlfredApiAbstractSerializer<QName> {

    private static final long serialVersionUID = 1L;

    public Jackson2AlfredApiQnameSerializer() {
        super(QName.class);
    }
}
