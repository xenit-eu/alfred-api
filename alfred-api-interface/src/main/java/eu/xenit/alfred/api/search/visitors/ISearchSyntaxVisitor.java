package eu.xenit.alfred.api.search.visitors;

import eu.xenit.alfred.api.search.nodes.InvertSearchNode;
import eu.xenit.alfred.api.search.nodes.OperatorSearchNode;
import eu.xenit.alfred.api.search.nodes.PropertySearchNode;
import eu.xenit.alfred.api.search.nodes.TermSearchNode;

/**
 * Interface used to enforce the visitor pattern on search nodes on its children.
 */
public interface ISearchSyntaxVisitor<T> {

    T visit(OperatorSearchNode n);

    T visit(PropertySearchNode n);

    T visit(TermSearchNode n);

    //T visit(RangeValue n);

    T visit(InvertSearchNode invertSearchNode);
}
