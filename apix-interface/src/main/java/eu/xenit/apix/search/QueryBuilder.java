package eu.xenit.apix.search;

import eu.xenit.apix.search.nodes.*;
import eu.xenit.apix.utils.java8.Consumer;

import java.util.ArrayList;

/**
 * Object used to build a search query.
 */
public class QueryBuilder {

    // Parent builder to return to when exiting nesting
    QueryBuilder parent = null;
    // What should be done with the created node when calling a .xxx on the builder,
    // Sort-of continuation taking the result node of calling a .xxx on the builder
    Consumer<SearchSyntaxNode> acceptNode = null;
    // The result node built by this querybuilder
    SearchSyntaxNode result = null;

    public QueryBuilder() {
//        acceptNode = x -> {
//            result = x;
//            acceptNode = null;
//        };
        acceptNode = new Consumer<SearchSyntaxNode>() {
            @Override
            public void accept(SearchSyntaxNode searchSyntaxNode) {
                result = searchSyntaxNode;
                acceptNode = null;
            }
        };
    }

    public QueryBuilder(QueryBuilder parent, Consumer<SearchSyntaxNode> acceptNode, SearchSyntaxNode result) {
        this.parent = parent;
        this.acceptNode = acceptNode;
        this.result = result;
    }

    public QueryBuilder term(String name, String val) {
        acceptNode.accept(new TermSearchNode(name, val));
        return this;
    }

    public SearchSyntaxNode create() {
        if (parent != null) {
            throw new UnsupportedOperationException();
        }
        if (acceptNode != null) {
            throw new UnsupportedOperationException();
        }
        if (result == null) {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    /**
     * Add a property to the query. This is not an exact match.
     *
     * @param qname The qname of the property that should be queried.
     * @param val The value of the property that should be queried.
     * @return a new QueryBuilder which also contains the new property.
     */
    public QueryBuilder property(String qname, String val) {
        return property(qname, val, false);
    }

    /**
     * Add a property to the query.
     *
     * @param qname The qname of the property that should be queried.
     * @param val The value of the property that should be queried.
     * @param exactMatch Whether the query should be an exact match or not.
     * @return a new QueryBuilder which also contains the new property.
     */
    public QueryBuilder property(String qname, String val, boolean exactMatch) {
        PropertySearchNode t = new PropertySearchNode(qname, val);
        t.setExact(exactMatch);
        acceptNode.accept(t);
        return this;
    }

    /**
     * Add a property with a range value to the query.
     *
     * @param qname The qname of the property that should be queried.
     * @param start The start of the value of the range on which is searched.
     * @param end The end of the value of the range on which is searched.
     * @return a new QueryBuilder which also contains the new property on which is search with the given range.
     */
    public QueryBuilder property(String qname, String start, String end) {
        RangeValue range = new RangeValue(start, end);
        PropertySearchNode t = new PropertySearchNode(qname, range);
        acceptNode.accept(t);
        return this;
    }

    /**
     * Starts the building of an and query.
     *
     * @return a new QueryBuilder which also contains the and query under construction.
     */
    public QueryBuilder startAnd() {
        OperatorSearchNode.Operator operator = OperatorSearchNode.Operator.AND;
        return startOperator(operator);
    }

    private QueryBuilder startOperator(OperatorSearchNode.Operator operator) {
        final OperatorSearchNode node = new OperatorSearchNode(operator, new ArrayList<SearchSyntaxNode>());

        QueryBuilder subBuilder = new QueryBuilder();
        subBuilder.result = node;
//        subBuilder.acceptNode = x -> node.getChildren().add(x);
        subBuilder.acceptNode = new Consumer<SearchSyntaxNode>() {
            @Override
            public void accept(SearchSyntaxNode searchSyntaxNode) {
                node.getChildren().add(searchSyntaxNode);
            }
        };
        subBuilder.parent = this;

        return subBuilder;
    }

    /**
     * Closes the current query. For example: In case you started an or query within the current query this closes the
     * or query.
     *
     * @return a new QueryBuilder in which the current query is closed.
     */
    public QueryBuilder end() {
        parent.acceptNode.accept(result);
        return parent;
    }

    /**
     * Starts the building of an and query.
     *
     * @return a new QueryBuilder which also contains the and query under construction.
     */
    public QueryBuilder startOr() {
        return startOperator(OperatorSearchNode.Operator.OR);
    }

    /**
     * Starts the building of an not query.
     *
     * @return a new QueryBuilder which also contains the and query under construction.
     */
    public QueryBuilder not() {

        final Consumer<SearchSyntaxNode> oldAccept = acceptNode;

//        acceptNode = x -> {
//            // This has to be before accept, non-elegant solution, because the result-accept sets acceptNode to 0
//            acceptNode = oldAccept;
//            oldAccept.accept(new InvertSearchNode(x));
//
//        };

        acceptNode = new Consumer<SearchSyntaxNode>() {
            @Override
            public void accept(SearchSyntaxNode searchSyntaxNode) {
                acceptNode = oldAccept;
                oldAccept.accept(new InvertSearchNode(searchSyntaxNode));
            }
        };

        return this;
    }
}
