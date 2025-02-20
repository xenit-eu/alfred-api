package eu.xenit.alfred.api.tests.search;

import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.visitors.SearchSyntaxPrinter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryBuilderTest {

    private final static Logger logger = LoggerFactory.getLogger(QueryBuilderTest.class);

    @Test
    public void TestComplex() {
        QueryBuilder builder = new QueryBuilder();
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

        logger.debug(SearchSyntaxPrinter.Print(node));

        Assert.assertEquals(
                "NOT AND(NOT TERM aspect=a, PROP cm:content=lala, NOT PROP cm:content=bye, OR(TERM aspect=c))",
                SearchSyntaxPrinter.Print(node));


    }

}