package eu.xenit.alfred.api.search.visitors;

import eu.xenit.alfred.api.search.nodes.InvertSearchNode;
import eu.xenit.alfred.api.search.nodes.OperatorSearchNode;
import eu.xenit.alfred.api.search.nodes.PropertySearchNode;
import eu.xenit.alfred.api.search.nodes.RangeValue;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.nodes.TermSearchNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Search syntax visitor that prints the search nodes into human readable format.
 */
public class SearchSyntaxPrinter extends BaseSearchSyntaxNodeVisitor<String> {

    public static String Print(SearchSyntaxNode node) {
        SearchSyntaxPrinter p = new SearchSyntaxPrinter();
        return p.visit(node);
    }

    @Override
    public String visit(OperatorSearchNode n) {

        String childs;
        if (n.getChildren().size() == 0) {
            return "(NONE)";
        } else {
            //childs = n.getChildren().stream().map(a -> a.accept(this)).reduce((a, b) -> a + ", " + b).get();

            List<String> prints = new ArrayList<>(n.getChildren().size());
            for (SearchSyntaxNode searchSyntaxNode : n.getChildren()) {
                prints.add(searchSyntaxNode.accept(this));
            }

            Boolean first = true;
            childs = "";
            for (String print : prints) {
                if (first) {
                    childs = print;
                    first = false;
                } else {
                    childs = childs + ", " + print;
                }
            }

        }
        return n.getOperator().toString() + "(" + childs + ")";
    }

    @Override
    public String visit(PropertySearchNode n) {
        String val = "";
        if (n.getValue() != null) {
            val += n.getValue();
        }
        if (n.getRange() != null) {
            val += toString(n.getRange());
        }

        return String.format("PROP %s=%s", n.getName(), val);
    }

    @Override
    public String visit(TermSearchNode n) {
        return String.format("TERM %s=%s", n.getTerm(), n.getValue());
    }

    public String toString(RangeValue n) {
        String from = "";
        String to = "";
        if (n.getStart() != null) {
            from = "FROM " + n.getStart();
        }
        if (n.getEnd() != null) {
            to = "TO " + n.getEnd();
        }
        if (from != "" && to != "") {
            to = " " + to;
        }
        return String.format("RANGE [%s%s]", from, to);
    }

    @Override
    public String visit(InvertSearchNode invertSearchNode) {
        return "NOT " + visit(invertSearchNode.getTarget());
    }

}
