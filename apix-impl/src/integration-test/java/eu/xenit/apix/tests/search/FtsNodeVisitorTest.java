package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco.search.FtsNodeVisitor;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorTest.class);

    private QueryBuilder builder;

    @Before
    public void Setup() {
        builder = new QueryBuilder();
    }


    @Test
    public void TestTerm() {
        java.util.List<String> terms = Arrays.asList("type", "aspect", "path", "parent", "text", "category");
        for (String t : terms) {
            builder = new QueryBuilder();
            SearchSyntaxNode node = builder.term(t, "myVal").create();
            Assert.assertEquals(t.toUpperCase() + ":\"myVal\"", toFts(node));
        }

        builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("noderef", "workspace://SpacesStore/some-uuid").create();
        Assert.assertEquals("ID:\"workspace://SpacesStore/some-uuid\"", toFts(node));
    }

    @Test
    public void TestTermALL() {
        builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("all", "fred").create();

        Assert.assertEquals(
                "(TEXT:\"fred\" OR cm:name:\"fred\" OR cm:author:\"fred\" OR cm:creator:\"fred\" OR cm:modifier:\"fred\")",
                toFts(node));
    }

    @Test
    public void TestTermEscaped() {
        String t = "type";
        SearchSyntaxNode node = builder.term(t, "my\"Val").create();
        Assert.assertEquals(t.toUpperCase() + ":\"my\\\"Val\"", toFts(node));
    }

    @Test
    public void TestProperty() {
        String t = "type";
        SearchSyntaxNode node = builder.property("cm:content", "myContent").create();
        Assert.assertEquals("cm:content:\"myContent\"", toFts(node));
    }

    @Test
    public void TestPropertyEscaped() {
        SearchSyntaxNode node = builder.property("cm:content", "my\"Content").create();
        Assert.assertEquals("cm:content:\"my\\\"Content\"", toFts(node));
    }


    @Test
    public void TestAnd() {
        SearchSyntaxNode node = builder.startAnd().term("type", "myType").term("aspect", "myAspect")
                .term("aspect", "myAspect2").end().create();
        Assert.assertEquals("(TYPE:\"myType\" AND ASPECT:\"myAspect\" AND ASPECT:\"myAspect2\")", toFts(node));
    }

    @Test
    public void TestOr() {
        SearchSyntaxNode node = builder.startOr().term("type", "myType").term("aspect", "myAspect")
                .term("aspect", "myAspect2").end().create();
        Assert.assertEquals("(TYPE:\"myType\" OR ASPECT:\"myAspect\" OR ASPECT:\"myAspect2\")", toFts(node));
    }

    @Test
    public void TestNot() {
        SearchSyntaxNode node = builder.not().term("type", "myType").create();
        Assert.assertEquals("NOT TYPE:\"myType\"", toFts(node));
    }

    @Test
    public void TestNested() {
        SearchSyntaxNode node =
                builder.not().startAnd()
                        .not().term("aspect", "a")
                        .property("cm:content", "lala")
                        .not().property("cm:content", "bye")
                        .startOr()
                        .term("aspect", "c")
                        .end()
                        .end()
                        .create();

       /* SearchSyntaxNode node =  builder.start
        assertEquals("(TYPE:myType OR ASPECT:myAspect OR ASPECT:myAspect2)",toFts(node));*/
    }

    @Test
    public void TestAll_And() throws IOException {
        SearchSyntaxNode node =
                builder.startAnd()
                        .term("all", "hello")
                        .term("type", "cm:content")
                        .end()
                        .create();
        //SearchNodeJsonParser parser = new SearchNodeJsonParser();
        //SearchSyntaxNode val = parser.ParseJSON("{\"not\":{\"property\":{\"name\":\"@{claims.model}duplicate\",\"value\":\"true\"}}}");

        logger.debug(toFts(node));
        Assert.assertEquals(
                "((TEXT:\"hello\" OR cm:name:\"hello\" OR cm:author:\"hello\" OR cm:creator:\"hello\" OR cm:modifier:\"hello\") AND TYPE:\"cm:content\")",
                toFts(node));

    }

    @Test
    public void TestAll_And_SingleElement() throws IOException {
        SearchSyntaxNode node =
                builder.startAnd()
                        .term("type", "cm:content")
                        .end()
                        .create();
        //SearchNodeJsonParser parser = new SearchNodeJsonParser();
        //SearchSyntaxNode val = parser.ParseJSON("{\"not\":{\"property\":{\"name\":\"@{claims.model}duplicate\",\"value\":\"true\"}}}");

        logger.debug(toFts(node));
        //THERE SHOULD NOT BE ANY ADDITIONAL PARENTHESIS!
        Assert.assertEquals("TYPE:\"cm:content\"", toFts(node));

    }

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
