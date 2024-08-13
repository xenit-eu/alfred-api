package eu.xenit.alfred.api.search.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.xenit.alfred.api.search.nodes.InvertSearchNode;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import java.io.IOException;

public class InvertSearchNodeDeserializer extends JsonDeserializer<InvertSearchNode> {

    @Override
    public InvertSearchNode deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        SearchSyntaxNode nested = (SearchSyntaxNode) ctxt
                .findRootValueDeserializer(ctxt.constructType(SearchSyntaxNode.class)).deserialize(jp, ctxt);

        return new InvertSearchNode(nested);

    }
}
