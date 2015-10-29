package eu.xenit.apix.search.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.xenit.apix.search.nodes.InvertSearchNode;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import java.io.IOException;

public class SearchNodeDeserializer extends JsonDeserializer<SearchSyntaxNode> {

    @Override
    public SearchSyntaxNode deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new UnsupportedOperationException();
        }

        String field = jp.nextFieldName();

        com.fasterxml.jackson.databind.JavaType type = ctxt.constructType(InvertSearchNode.class);

        JsonDeserializer<Object> des = ctxt.findRootValueDeserializer(type);
        //jp.nextToken();
        //jp.nextValue();

        return (SearchSyntaxNode) des.deserialize(jp, ctxt);
    }
}
