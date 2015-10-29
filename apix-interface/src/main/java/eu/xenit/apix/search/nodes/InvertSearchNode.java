package eu.xenit.apix.search.nodes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.xenit.apix.search.json.InvertSearchNodeDeserializer;
import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;

/**
 * Search node that represents an invert (logical not) operation to its children.
 */
@JsonDeserialize(using = InvertSearchNodeDeserializer.class)
public class InvertSearchNode implements SearchSyntaxNode {


    private SearchSyntaxNode target;

    public InvertSearchNode() {
    }

    public InvertSearchNode(SearchSyntaxNode target) {
        this.target = target;
    }

    //@Override
    public <T> T accept(ISearchSyntaxVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public SearchSyntaxNode getTarget() {
        return target;
    }


    public void setTarget(SearchSyntaxNode target) {
        this.target = target;
    }
}
