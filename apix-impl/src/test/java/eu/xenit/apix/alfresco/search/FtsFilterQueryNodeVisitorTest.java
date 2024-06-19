package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.ISearchSyntaxVisitor;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import java.util.List;


public class FtsFilterQueryNodeVisitorTest {

    private static final SearchSyntaxNode querySyntaxTree = new QueryBuilder()
            .startAnd()
            .property(
                    "{http://www.alfresco.org/model/content/1.0}created",
                    "NOW/DAY-1DAY\"..\"NOW/DAY+1DAY"
            )
            .term("type", "{http://www.alfresco.org/model/content/1.0}content")
            .end()
            .create();

    private static final String ftsQuery = querySyntaxTree.accept(new FtsNodeVisitor());
    private static final String creatorFacetField = "@{http://www.alfresco.org/model/content/1.0}created";

    @Test
    public void alfrescoHasBuggySolrFacetHelper() {

        SolrFacetHelper helper = new SolrFacetHelper(new ArrayList<>());

        // ftsQuery =
        // ({http://www.alfresco.org/model/content/1.0}created:"NOW/DAY-1DAY".."NOW/DAY+1DAY" AND
        //  TYPE:"{http://www.alfresco.org/model/content/1.0}content")
        String buggyResult = helper.createFacetQueriesFromSearchQuery(creatorFacetField, ftsQuery);

        String expectedResult = "@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]";

        // As soon as this assertion starts failing, Alfresco fixed this bug
        Assertions.assertNotEquals(expectedResult, buggyResult);
    }

    @Test
    public void testCreatedByValue() {
        ISearchSyntaxVisitor<List<String>> visitor = new FtsFilterQueryNodeVisitor(creatorFacetField);

        List<String> filterQueries = querySyntaxTree.accept(visitor);

        Assertions.assertSame(1, filterQueries.size());
        Assertions.assertEquals("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]",
                filterQueries.get(0));
    }

    @Test
    public void testCreatedByRange() {
        ISearchSyntaxVisitor<List<String>> visitor = new FtsFilterQueryNodeVisitor(creatorFacetField);

        SearchSyntaxNode search = new QueryBuilder()
                .startAnd()
                .property(
                        "{http://www.alfresco.org/model/content/1.0}created",
                        "NOW/DAY-1DAY",
                        "NOW/DAY+1DAY"
                )
                .term("type", "{http://www.alfresco.org/model/content/1.0}content")
                .end()
                .create();

        List<String> filterQueries = querySyntaxTree.accept(visitor);

        Assertions.assertSame(1, filterQueries.size());
        Assertions.assertEquals("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]",
                filterQueries.get(0));
    }


    @Test
    public void testMultipleMatchingNodes() {
        ISearchSyntaxVisitor<List<String>> visitor = new FtsFilterQueryNodeVisitor(creatorFacetField);

        // fictive search query for: today OR exactly 1 month ago
        SearchSyntaxNode search = new QueryBuilder()
                .startAnd()
                .startOr()
                .property(
                        "{http://www.alfresco.org/model/content/1.0}created",
                        "NOW/DAY-1DAY\"..\"NOW/DAY+1DAY")
                .property(
                        "{http://www.alfresco.org/model/content/1.0}created",
                        "NOW/DAY-1MONTH-1DAY",
                        "NOW/DAY-1MONTH+1DAY")
                .end()
                .term("type", "{http://www.alfresco.org/model/content/1.0}content")
                .end()
                .create();

        List<String> filterQueries = search.accept(visitor);

        Assertions.assertSame(2, filterQueries.size());
        Assertions.assertEquals("@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]",
                filterQueries.get(0));
        Assertions.assertEquals(
                "@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1MONTH-1DAY TO NOW/DAY-1MONTH+1DAY]",
                filterQueries.get(1));
    }
}