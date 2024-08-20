package eu.xenit.alfred.api.tests.search;

import eu.xenit.alfred.api.search.nodes.OperatorSearchNode;
import eu.xenit.alfred.api.search.nodes.PropertySearchNode;
import eu.xenit.alfred.api.search.nodes.RangeValue;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.nodes.TermSearchNode;
import eu.xenit.alfred.api.search.visitors.SearchSyntaxPrinter;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchSyntaxPrinterTest {

    private final static Logger logger = LoggerFactory.getLogger(SearchSyntaxPrinterTest.class);

    @Test
    public void TestPrinter() {

        ArrayList<SearchSyntaxNode> childs = new ArrayList<SearchSyntaxNode>();
        OperatorSearchNode node = new OperatorSearchNode(OperatorSearchNode.Operator.AND, childs);

        childs.add(new PropertySearchNode("cm:content", "value"));

        OperatorSearchNode nodeOr = new OperatorSearchNode(OperatorSearchNode.Operator.OR, null);
        childs.add(nodeOr);
        childs = new ArrayList<>();
        nodeOr.setChildren(childs);

        childs.add(new TermSearchNode("aspectname", "aspect"));
        childs.add(new PropertySearchNode("cm:modified", new RangeValue("now", "future")));

        SearchSyntaxPrinter printer = new SearchSyntaxPrinter();
        String val = printer.visit(node);
        logger.debug(val);

        Assert.assertEquals(
                "AND(PROP cm:content=value, OR(TERM aspectname=aspect, PROP cm:modified=RANGE [FROM now TO future]))",
                val);
    }
}