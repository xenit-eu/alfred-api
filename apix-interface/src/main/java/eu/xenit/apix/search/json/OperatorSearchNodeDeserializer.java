package eu.xenit.apix.search.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.xenit.apix.search.nodes.OperatorSearchNode;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michiel Huygen on 10/11/2015.
 */
public class OperatorSearchNodeDeserializer extends JsonDeserializer<OperatorSearchNode> {


    @Override
    public OperatorSearchNode deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        // Anti-hack for the 'hack' in AsWrapperType, remove 'nodeType' field again
        if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new UnsupportedOperationException();
        }

        List<SearchSyntaxNode> nodes = new ArrayList<>();
        while (jp.nextToken() != JsonToken.END_ARRAY) {
// We are in an array element
            SearchSyntaxNode nested = (SearchSyntaxNode) ctxt
                    .findRootValueDeserializer(ctxt.constructType(SearchSyntaxNode.class)).deserialize(jp, ctxt);
            nodes.add(nested);
        }
        // Type is set using the IJsonTyped interface
        return new OperatorSearchNode(null, nodes);

    }


}
