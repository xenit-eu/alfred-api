package eu.xenit.alfred.api.alfresco.search;

import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorUnitTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorUnitTest.class);

    private QueryBuilder builder;

    @BeforeEach
    public void Setup() {
        builder = new QueryBuilder();
    }


    @Test
    public void TestTerm() {
        java.util.List<String> terms = Arrays.asList("type", "aspect", "path", "parent", "text", "category");
        for (String t : terms) {
            builder = new QueryBuilder();
            SearchSyntaxNode node = builder.term(t, "myVal").create();
            Assertions.assertEquals(t.toUpperCase() + ":\"myVal\"", toFts(node));
        }

        builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("noderef", "workspace://SpacesStore/some-uuid").create();
        Assertions.assertEquals("ID:\"workspace://SpacesStore/some-uuid\"", toFts(node));
    }

    @Test
    public void TestTermALL() {
        builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("all", "fred").create();

        Assertions.assertEquals(
                "(TEXT:\"fred\" OR cm:name:\"fred\" OR cm:author:\"fred\" OR cm:creator:\"fred\" OR cm:modifier:\"fred\")",
                toFts(node));
    }

    @Test
    public void TestTermEscaped() {
        String t = "type";
        SearchSyntaxNode node = builder.term(t, "my\"Val").create();
        Assertions.assertEquals(t.toUpperCase() + ":\"my\\\"Val\"", toFts(node));
    }

    @Test
    public void TestProperty() {
        String t = "type";
        SearchSyntaxNode node = builder.property("cm:content", "myContent").create();
        Assertions.assertEquals("cm:content:\"myContent\"", toFts(node));
    }

    @Test
    public void TestPropertyEscaped() {
        SearchSyntaxNode node = builder.property("cm:content", "my\"Content").create();
        Assertions.assertEquals("cm:content:\"my\\\"Content\"", toFts(node));
    }


    @Test
    public void TestAnd() {
        SearchSyntaxNode node = builder.startAnd().term("type", "myType").term("aspect", "myAspect")
                .term("aspect", "myAspect2").end().create();
        Assertions.assertEquals("(TYPE:\"myType\" AND ASPECT:\"myAspect\" AND ASPECT:\"myAspect2\")", toFts(node));
    }

    @Test
    public void TestOr() {
        SearchSyntaxNode node = builder.startOr().term("type", "myType").term("aspect", "myAspect")
                .term("aspect", "myAspect2").end().create();
        Assertions.assertEquals("(TYPE:\"myType\" OR ASPECT:\"myAspect\" OR ASPECT:\"myAspect2\")", toFts(node));
    }

    @Test
    public void TestNot() {
        SearchSyntaxNode node = builder.not().term("type", "myType").create();
        Assertions.assertEquals("NOT TYPE:\"myType\"", toFts(node));
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
        Assertions.assertEquals(
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
        Assertions.assertEquals("TYPE:\"cm:content\"", toFts(node));

    }

    private String toFts(SearchSyntaxNode node) {
        logger.debug(SearchSyntaxPrinter.Print(node));
        FtsNodeVisitor visitor = new FtsNodeVisitor();
        String ret = visitor.visit(node);

        logger.debug(ret);
        return ret;
    }
}
