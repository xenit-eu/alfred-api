package eu.xenit.apix.search.visitors;

import eu.xenit.apix.search.nodes.InvertSearchNode;
import eu.xenit.apix.search.nodes.OperatorSearchNode;
import eu.xenit.apix.search.nodes.PropertySearchNode;
import eu.xenit.apix.search.nodes.TermSearchNode;

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
