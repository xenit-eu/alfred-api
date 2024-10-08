package eu.xenit.alfred.api.search.nodes;

import eu.xenit.alfred.api.search.visitors.ISearchSyntaxVisitor;

/**
 * Represents a search node that searches for a property a specific value or value range.
 */
public class PropertySearchNode implements SearchSyntaxNode {

    private static final String ESCAPE_GROUP_REGEX = "([!@%^&*()\\-=+\\[\\];?,<>|])";

    private String name;
    private String value;
    private RangeValue range;
    private boolean exact = false;


    public PropertySearchNode() {
    }

    public PropertySearchNode(String name, String value, String exact) {
        this(name, value);
        this.exact = Boolean.parseBoolean(exact);
    }

    public PropertySearchNode(String name, String value) {
        setName(name);
        this.value = value;
        this.range = null;
    }

    public PropertySearchNode(String name, RangeValue range) {
        setName(name);
        this.range = range;
        this.value = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Solr queries where the property name contains any of the characters in the regex below trigger an error
        // in solr. This error is propagated through alfresco and alfredApi. To prevent this, we escape the characters
        // before the terms enter solr.
        this.name = escapeName(name);
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

    public static String escapeName(String name) {
        return name.replaceAll(ESCAPE_GROUP_REGEX, "\\\\$1");
    }

    public static String unescapeName(String name) {
        return name.replaceAll("\\\\" + ESCAPE_GROUP_REGEX, "$1");
    }
}
