package eu.xenit.alfred.api.search.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import eu.xenit.alfred.api.search.nodes.InvertSearchNode;
import eu.xenit.alfred.api.search.nodes.OperatorSearchNode;
import eu.xenit.alfred.api.search.nodes.PropertySearchNode;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.nodes.TermSearchNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class SearchNodeJsonParser {

    private final ObjectMapper mapper;

    public SearchNodeJsonParser() {
        mapper = new ObjectMapper();
        configureObjectMapper();
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public SearchSyntaxNode ParseJSON(String json) throws IOException {
        json = json.replaceAll("'", "\"");
        return mapper.readValue(json, SearchSyntaxNode.class);
    }

    private ObjectMapper configureObjectMapper() {
        mapper.setSubtypeResolver(new CustomSubtypeResolver());

        // TODO: improve Type configuration location, use annotation in nodes?

        for (String term : newArrayList("type", "aspect", "noderef", "path", "text", "parent", "category",
                "all", "isunset", "isnull", "isnotnull", "exists")) {
            mapper.registerSubtypes(new NamedType(TermSearchNode.class, term));
        }
        for (String operand : newArrayList("and", "or")) {
            mapper.registerSubtypes(new NamedType(OperatorSearchNode.class, operand));
        }
        mapper.registerSubtypes(new NamedType(PropertySearchNode.class, "property"));
        mapper.registerSubtypes(new NamedType(InvertSearchNode.class, "not"));
        return mapper;
    }

    private ArrayList<String> newArrayList(String... elements) {
        ArrayList<String> ret = new ArrayList<String>();
        Collections.addAll(ret, elements);

        return ret;
    }

    /*public SearchSyntaxNode ParseJSON(JsonNode node) {

        //TODO:
        return null;

    }*/
}
