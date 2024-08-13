package eu.xenit.alfred.api.tests.search;

import eu.xenit.alfred.api.alfresco.search.FtsNodeVisitor;
import eu.xenit.alfred.api.search.json.SearchNodeJsonParser;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorTest.class);

    @Test
    public void TestTemp() throws IOException {
        SearchNodeJsonParser parser = new SearchNodeJsonParser();
        SearchSyntaxNode val = parser
                .ParseJSON("{\"not\":{\"property\":{\"name\":\"@{claims.model}duplicate\",\"value\":\"true\"}}}");

        logger.debug(toFts(val));
    }


    /**
     * Past problem where additional parenthesis were added for a single-element and term, breaking the fts not
     * operator
     */
    @Test
    public void TestNotProblem() throws IOException {
        String query = "{\n" +
                "      \"and\":[\n" +
                "         {\n" +
                "            \"and\":[\n" +
                "               {\n" +
                "                  \"not\":{\n" +
                "                     \"and\":[\n" +
                "                        {\n" +
                "                           \"property\":{\n" +
                "                              \"name\":\"{http://www.alfresco.org/model/content/1.0}content.mimetype\",\n"
                +
                "                              \"value\":\"message/rfc822\"\n" +
                "                           }\n" +
                "                        },\n" +
                "                        {\"property\":{\"name\":\"{pv.model}originator\",\"value\":\"Captiva\"}}\n" +
                "                     ]\n" +
                "                  }\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"and\":[\n" +
                "               {\n" +
                "                  \"and\":[\n" +
                "                     {\"property\":{\"name\":\"{pv.model}producerNumber\",\"value\":\"002818\"}}\n" +
                "                  ]\n" +
                "               }\n" +
                "            ]\n" +
                "         }\n" +
                "      ]\n" +
                "}";

        //WAS => FTS DOESNT SUPPORT () AROUND NOT "((NOT ({http://www.alfresco.org/model/content/1.0}content.mimetype:\"message/rfc822\" AND {pv.model}originator:\"Captiva\"0)) AND (({pv.model}producerNumber:\"002818\")))"

        String expected = "(NOT ({http://www.alfresco.org/model/content/1.0}content.mimetype:\"message/rfc822\" AND {pv.model}originator:\"Captiva\") AND {pv.model}producerNumber:\"002818\")";
        SearchNodeJsonParser parser = new SearchNodeJsonParser();
        SearchSyntaxNode val = parser.ParseJSON(query);

        Assert.assertEquals(expected, toFts(val));
    }


    private String toFts(SearchSyntaxNode node) {
        logger.debug(SearchSyntaxPrinter.Print(node));
        FtsNodeVisitor visitor = new FtsNodeVisitor();
        String ret = visitor.visit(node);

        logger.debug(ret);
        return ret;
    }
}
