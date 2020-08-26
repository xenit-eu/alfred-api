package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.nodes.*;
import eu.xenit.apix.search.visitors.BaseSearchSyntaxNodeVisitor;
import eu.xenit.apix.utils.StringUtils;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stan on 16-Feb-16.
 */
public class LuceneNodeVisitor extends BaseSearchSyntaxNodeVisitor<String> {

    private StringBuilder builder = new StringBuilder();
    private HashMap<String, String> termDictionary = new HashMap<>();

    public LuceneNodeVisitor() {
        termDictionary.put("type", "TYPE");
        termDictionary.put("aspect", "ASPECT");
        termDictionary.put("noderef", "ID");
        termDictionary.put("parent", "PARENT");
        termDictionary.put("path", "PATH");
        termDictionary.put("category", "CATEGORY");
        termDictionary.put("text", "TEXT");
        termDictionary.put("all", "ALL");
        termDictionary.put("isunset", "ISUNSET");
        termDictionary.put("isnull", "ISNULL");
        termDictionary.put("isnotnull", "ISNOTNULL");
        termDictionary.put("exists", "EXISTS");
        //termDictionary.put("","");
    }

    @Override
    public String visit(OperatorSearchNode n) {

        List<String> children = new ArrayList<>(n.getChildren().size());
        for (SearchSyntaxNode node : n.getChildren()) {
            children.add(visit(node));
        }

//        String s = String.join(" " + n.getOperator() + " ", n.getChildren().stream().map(el -> visit(el)).collect(Collectors.toList()));
        String s = StringUtils.join(" " + n.getOperator() + " ", children);
        builder.setLength(0);
        builder.append('(');
        builder.append(s);
        builder.append(')');

        return builder.toString();
    }

    @Override
    public String visit(PropertySearchNode n) {
        builder.setLength(0);
        builder.append("@");
        builder.append(escape(n.getName()));
        builder.append(':');
        if (n.getValue() != null && n.getRange() != null) {
            throw new UnsupportedOperationException("Property can't have both range AND value!");
        }
        if (n.getValue() != null) {
            builder.append('"');
            builder.append(escape(n.getValue()));
            builder.append('"');
        }
        if (n.getRange() != null) {
            builder.append(toRange(n.getRange()));
        }

        return builder.toString();
    }

    private String toRange(RangeValue n) {
        if (n.getStart() == null && n.getEnd() == null) {
            throw new UnsupportedOperationException("Start OR end is required for ranges");
        }
        String start = "MIN";
        String end = "MAX";
        if (n.getStart() != null) {
            start = '"' + escape(n.getStart()) + '"';
        }
        if (n.getEnd() != null) {
            end = '"' + escape(n.getEnd()) + '"';
        }

        return '[' + n.getStart() + " TO " + n.getEnd() + ']';
    }

    @Override
    public String visit(TermSearchNode n) {
        if (n.getTerm() == "all") {
            String escaped = escape(n.getValue());

            return String.format(
                    "(TEXT:\"%s\" OR @cm\\:name:\"%s\" OR @cm\\:author:\"%s\" OR @cm\\:creator:\"%s\" OR @cm\\:modifier:\"%s\")",
                    escaped,
                    escaped,
                    escaped,
                    escaped,
                    escaped);

        }
        builder.setLength(0);
        builder.append(translateTerm(n.getTerm()));
        builder.append(':');
        builder.append('"');
        builder.append(escape(n.getValue()));
        builder.append('"');
        return builder.toString();
    }

    @Override
    public String visit(InvertSearchNode invertSearchNode) {
        // if there is only a single node, the query is not supported.
        return "-" + visit(invertSearchNode.getTarget());
    }

    public String translateTerm(String term) {
        term = term.toLowerCase();
        if (!termDictionary.containsKey(term)) {
            throw new UnsupportedOperationException("Search term not supported in FTS!");
        }

        return termDictionary.get(term);

    }

    private String escape(String input) {
        return LuceneQueryParser.escape(input);
    }
}
