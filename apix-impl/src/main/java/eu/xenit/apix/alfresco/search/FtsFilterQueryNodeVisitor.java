package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.nodes.*;
import eu.xenit.apix.search.visitors.BaseSearchSyntaxNodeVisitor;
import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This -visitor extracts all terms that matchs a given filter from a search syntax-tree.
 *
 * This code replaces the (buggy) functionality in {@link org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper#createFacetQueriesFromSearchQuery(String
 * field, String searchQuery) createFacetQueriesFromSearchQuery} It's used when creating solr filter-queries for filter
 * buckets/ranges, when the solr-query contains a term that matches a filter-field.
 *
 * See https://xenitsupport.jira.com/browse/ALFREDAPI-347
 */
public class FtsFilterQueryNodeVisitor implements ISearchSyntaxVisitor<List<String>> {

    private final String field;

    public FtsFilterQueryNodeVisitor(String field) {

        if (field.startsWith("@")) {
            field = field.substring(1);
        }

        this.field = field;
    }

    public List<String> visit(SearchSyntaxNode node) {
        return node.accept(this);
    }

    @Override
    public List<String> visit(OperatorSearchNode n) {
        List<String> result = new ArrayList<>();
        for (SearchSyntaxNode child : n.getChildren()) {
            result.addAll(this.visit(child));
        }
        return result;
    }

    @Override
    public List<String> visit(PropertySearchNode n) {
        if (!this.field.equalsIgnoreCase(n.getName())) {
            return Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(n.getName());
        sb.append(':');

        if (n.getRange() != null) {
            sb.append("[");
            sb.append(n.getRange().getStart() == null ? "MIN" : n.getRange().getStart());
            sb.append(" TO ");
            sb.append(n.getRange().getEnd() == null ? "MAX" : n.getRange().getEnd());
            sb.append("]");
        } else if (n.getValue() != null) {
            // convert value NOW/DAY-1DAY".."NOW/DAY+1DAY
            // into [NOW/DAY-1DAY TO NOW/DAY+1DAY]
            sb.append("[");

            String range = n.getValue();
            // replace the middle '".."' (including inner quotations marks)
            range = range.replace("\"..\"", " TO ");
            // remove the date-range quotations marks
            // -> not strictly required for how Finder uses facets, but it's safe
            range = range.replace("\"", "");

            sb.append(range);

            sb.append("]");

        } else {
            return Collections.emptyList();
        }

        return Collections.singletonList(sb.toString());

    }

    @Override
    public List<String> visit(TermSearchNode n) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visit(InvertSearchNode invertSearchNode) {
        return this.visit(invertSearchNode.getTarget());
    }
}
