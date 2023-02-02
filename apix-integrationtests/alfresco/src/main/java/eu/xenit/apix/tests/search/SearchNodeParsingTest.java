package eu.xenit.apix.tests.search;

import eu.xenit.apix.search.json.SearchNodeJsonParser;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchNodeParsingTest {

    private final static Logger logger = LoggerFactory.getLogger(SearchNodeParsingTest.class);

    private void assertJsonParsesInto(String json, String expected) throws IOException {
        logger.debug("Json: " + json);
        String parsed = toString(deserializeNode(json));
        logger.debug("Parsed: " + parsed);
        Assert.assertEquals(expected, parsed);
    }

    private SearchSyntaxNode deserializeNode(String json) throws IOException {
        return new SearchNodeJsonParser().ParseJSON(json);
    }

    private String toString(SearchSyntaxNode node) {
        SearchSyntaxPrinter printer = new SearchSyntaxPrinter();
        return printer.visit(node);
    }

    @Test
    public void TestTerms() throws IOException {
        for (String term : new String[]{"aspect", "type", "noderef", "path", "text", "parent", "category", "all"}) {
            assertJsonParsesInto("{'" + term + "':'myVal'}", "TERM " + term + "=myVal");
        }
    }

    @Test
    public void TestPropertyValue() throws IOException {
        assertJsonParsesInto("{'property': {'name':'cm:content','value':'myContent'} }",
                "PROP cm:content=myContent");
    }

    @Test
    public void TestPropertyRange() throws IOException {
        assertJsonParsesInto("{'property': {'name':'size','range': {'start':100,'end':200} } }",
                "PROP size=RANGE [FROM 100 TO 200]");
    }

    @Test
    public void TestPropertyRangeStart() throws IOException {
        assertJsonParsesInto("{'property': {'name':'size','range': {'start':100} } }",
                "PROP size=RANGE [FROM 100]");
    }

    @Test
    public void TestPropertyRangeEnd() throws IOException {
        assertJsonParsesInto("{'property': {'name':'size','range': {'end':200} } }",
                "PROP size=RANGE [TO 200]");
    }

    @Test
    public void TestNot() throws IOException {
        assertJsonParsesInto("{'not': {'aspect':'myAsp'} }",
                "NOT TERM aspect=myAsp");
    }


    @Test
    public void TestAnd() throws IOException {
        assertJsonParsesInto("{'and': [{'aspect': 'myAsp'},{'type': 'myType'}] }",
                "AND(TERM aspect=myAsp, TERM type=myType)");
    }

    @Test
    public void TestOr() throws IOException {
        assertJsonParsesInto("{'or': [{'aspect': 'myAsp'},{'type': 'myType'}] }",
                "OR(TERM aspect=myAsp, TERM type=myType)");
    }
}

