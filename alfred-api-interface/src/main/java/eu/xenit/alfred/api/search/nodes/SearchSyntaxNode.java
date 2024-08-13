package eu.xenit.alfred.api.search.nodes;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import eu.xenit.alfred.api.search.json.TypeResolver;
import eu.xenit.alfred.api.search.visitors.ISearchSyntaxVisitor;

/**
 * A search syntax node.
 */

// visible is set to true so that searchNodes can use the "nodeType" as a property
//@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM)
@JsonTypeResolver(TypeResolver.class)
//@JsonDeserialize(using = SearchNodeDeserializer.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, visible = true, property = "nodeType")
/*@JsonSubTypes({@JsonSubTypes.Type(value=InvertSearchNode.class,name="not"),
            @JsonSubTypes.Type(value=TermSearchNode.class,name="aspect")})*/
//@JsonIgnoreProperties(value = "nodeType",ignoreUnknown = true)
public interface SearchSyntaxNode {

    <T> T accept(ISearchSyntaxVisitor<T> visitor);
}
