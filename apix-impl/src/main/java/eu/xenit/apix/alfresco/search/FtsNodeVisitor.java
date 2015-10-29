package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.nodes.*;
import eu.xenit.apix.search.visitors.BaseSearchSyntaxNodeVisitor;
import eu.xenit.apix.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michiel Huygen on 12/11/2015.
 */
public class FtsNodeVisitor extends BaseSearchSyntaxNodeVisitor<String> {

    private StringBuilder builder = new StringBuilder();
    private HashMap<String, String> termToFtsTerm = new HashMap<>();

    public FtsNodeVisitor() {
        termToFtsTerm.put("type", "TYPE");
        termToFtsTerm.put("aspect", "ASPECT");
        termToFtsTerm.put("noderef", "ID");
        termToFtsTerm.put("parent", "PARENT");
        termToFtsTerm.put("path", "PATH");
        termToFtsTerm.put("category", "CATEGORY");
        termToFtsTerm.put("text", "TEXT");
        termToFtsTerm.put("all", "ALL");
        //termToFtsTerm.put("","");
    }


    @Override
    public String visit(OperatorSearchNode n) {
        //TODO: if it is only a single element, optimize!
        //TODO: if its empty, optimize!

        if (n.getChildren().size() == 1) {
            // FTS is very particular about its parentheses, putting parenthesis around a NOT breaks the query
            // Example:
            //   the term {not:x}   in fts becomes "NOT x"
            //   The term {and:[{not:x}]} is equivalent, however without this special case, it becomes "(NOT x)"
            // To make both cases the same, a single-element and is the same as the single-element

            return visit(n.getChildren().get(0));
        }

        //String s = ;
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
        if (n.isExact()) {
            builder.append('=');
        }
        builder.append(n.getName());
        builder.append(':');
        if (n.getValue() != null && n.getRange() != null) {
            throw new UnsupportedOperationException("Property can't have both range AND value!");
        }
        if (n.getValue() != null) {
            builder.append('"');
            builder.append(ftsEscape(n.getValue()));
            builder.append('"');
        }
        if (n.getRange() != null) {
            builder.append(toFts(n.getRange()));
        }

        return builder.toString();
    }

    @Override
    public String visit(TermSearchNode n) {

        if (n.getTerm() == "all") {
            String escaped = ftsEscape(n.getValue());

            return String.format(
                    "(TEXT:\"%s\" OR cm:name:\"%s\" OR cm:author:\"%s\" OR cm:creator:\"%s\" OR cm:modifier:\"%s\")",
                    escaped,
                    escaped,
                    escaped,
                    escaped,
                    escaped);

        }

        builder.setLength(0);
        builder.append(termToFtsTerm(n.getTerm()));
        builder.append(':');
        builder.append('"');
        builder.append(ftsEscape(n.getValue()));
        builder.append('"');
        return builder.toString();
    }


    public String toFts(RangeValue n) {
        if (n.getStart() == null && n.getEnd() == null) {
            throw new UnsupportedOperationException("Start OR end is required for ranges");
        }
        String start = "MIN";
        String end = "MAX";
        if (n.getStart() != null) {
            start = '"' + ftsEscape(n.getStart()) + '"';
        }
        if (n.getEnd() != null) {
            end = '"' + ftsEscape(n.getEnd()) + '"';
        }

        return '(' + start + ".." + end + ')';
    }


    @Override
    public String visit(InvertSearchNode invertSearchNode) {
        return "NOT " + visit(invertSearchNode.getTarget()) + "";
    }

    public String termToFtsTerm(String term) {
        term = term.toLowerCase();
        if (!termToFtsTerm.containsKey(term)) {
            throw new UnsupportedOperationException("Search term not supported in FTS! " + term);
        }

        return termToFtsTerm.get(term);

    }

    public String ftsEscape(String value) {
        //From fred client:
        //public static readonly String FTS_ESCAPE_TEXT = @"[\\+\-\!\(\)\:\^\[\]\{\}\~\*\?\" + '"' + "]";
        //public static readonly Regex FTS_PATTERN_TEXT = new Regex(FTS_ESCAPE_TEXT);
        //public static readonly String REPLACEMENT_STRING = @"\$0";
        return value.replaceAll("\"", "\\\"");
    }
}
