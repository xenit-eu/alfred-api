package eu.xenit.alfred.api.rest.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.xenit.alfred.api.data.NodeRef;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

public class Jackson2AlfredApiNodeRefDeserializer extends JsonDeserializer<NodeRef> implements Converter<String, NodeRef> {

    @Override
    public Class<?> handledType() {
        return NodeRef.class;
    }

    @Override
    public NodeRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return new NodeRef(
                jp.getText()
        );
    }

    @Override
    public NodeRef convert(String nodeRef) {
        return new NodeRef(nodeRef);
    }
}