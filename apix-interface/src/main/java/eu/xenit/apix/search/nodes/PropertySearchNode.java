package eu.xenit.apix.search.nodes;

import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;

/**
 * Represents a search node that searches for a property a specific value or value range.
 */
public class PropertySearchNode implements SearchSyntaxNode {

    private String name;
    private String value;
    private RangeValue range;
    private boolean exact = false;


    public PropertySearchNode() {
    }

    public PropertySearchNode(String name, String value) {
        this.name = name;
        this.value = value;
        this.range = null;
    }

    public PropertySearchNode(String name, RangeValue range) {
        this.name = name;
        this.range = range;
        this.value = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RangeValue getRange() {
        return range;
    }

    public void setRange(RangeValue range) {
        this.range = range;
    }

    @Override
    public <T> T accept(ISearchSyntaxVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public boolean isExact() {
        return exact;
    }

    public void setExact(boolean exact) {
        this.exact = exact;
    }
}
