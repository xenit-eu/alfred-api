package eu.xenit.alfred.api.search.visitors;

import eu.xenit.alfred.api.search.nodes.InvertSearchNode;
import eu.xenit.alfred.api.search.nodes.OperatorSearchNode;
import eu.xenit.alfred.api.search.nodes.PropertySearchNode;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.nodes.TermSearchNode;

/**
 * Abstract class that is a default "return null" implementation for ISearchSyntaxVisitor.
 */
public abstract class BaseSearchSyntaxNodeVisitor<T> implements ISearchSyntaxVisitor<T> {

    public T visit(SearchSyntaxNode node) {
        return node.accept(this);
    }

    @Override
    public T visit(OperatorSearchNode n) {
        return null;
    }

    @Override
    public T visit(PropertySearchNode n) {
        return null;
    }

    @Override
    public T visit(TermSearchNode n) {
        return null;
    }

    //@Override
    //public T visit(RangeValue n) {
    //    return null;
    //}

    @Override
    public T visit(InvertSearchNode invertSearchNode) {
        return null;
    }
}
