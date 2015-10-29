package eu.xenit.apix.search.nodes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.xenit.apix.search.json.IJsonTyped;
import eu.xenit.apix.search.json.OperatorSearchNodeDeserializer;
import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;
import java.util.List;

/**
 * Represents an AND or OR search node.
 */
@JsonDeserialize(using = OperatorSearchNodeDeserializer.class)
public class OperatorSearchNode implements SearchSyntaxNode, IJsonTyped {

    private Operator operator;
    private List<SearchSyntaxNode> children;

    public OperatorSearchNode(Operator operator, List<SearchSyntaxNode> children) {
        this.operator = operator;
        this.children = children;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<SearchSyntaxNode> getChildren() {
        return children;
    }

    public void setChildren(List<SearchSyntaxNode> children) {
        this.children = children;
    }

    @Override
    public <T> T accept(ISearchSyntaxVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void setTypeId(String id) {
        operator = OperatorSearchNode.Operator.valueOf(id.toUpperCase());
    }

    public enum Operator {
        AND, OR
    }

}
