package eu.xenit.apix.search.nodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.xenit.apix.search.json.IJsonTyped;
import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;

/**
 * Represents a search nodes that searches for a specific value for a specific term.
 */
public class TermSearchNode implements SearchSyntaxNode, IJsonTyped {

    private String term;
    private String value;

    @JsonCreator
    public TermSearchNode(String value) {
        this.value = value;
    }

    public TermSearchNode(@JsonProperty("nodeType") String term, @JsonProperty("value") String value) {
        this.value = value;
        this.term = term;
    }

    @Override
    public <T> T accept(ISearchSyntaxVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public void setTypeId(String id) {
        this.term = id;
    }
}
