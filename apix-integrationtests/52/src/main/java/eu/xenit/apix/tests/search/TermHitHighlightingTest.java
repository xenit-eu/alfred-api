package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.search.Highlights;
import eu.xenit.apix.search.Highlights.HighlightResult;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.HighlightOptions;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.util.SolrTestHelper;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TermHitHighlightingTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(TermHitHighlightingTest.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    /** Test all major parameters for term hit highlighting */
    public void searchResponseContainsHighlights() throws IOException, InterruptedException {
        // Waiting for Solr's indexing process to catch up before executing test.
        solrHelper.waitForSolrSync();
        SearchServiceTest.waitAWhile(20);

        URL expectedHighlightsJson = getClass().getClassLoader().getResource(
                "highlight-response-jsons/searchRequestWithHighlighting_withAllMajorCustomParams.json");
        Highlights expectedHighlightResult = mapper.readValue(expectedHighlightsJson, Highlights.class);

        /* The options set are equivalent to the following JSON:
            "query": {
                "property": {
                    "exact": false,
                    "name": "cm:content",
                    "value": "budget"
                }
            },
            "highlight": {
                "fields": [{"field": "cm:content"}],
                "prefix": "!PREFIX!",
                "postfix": "!SUFFIX!",
                "snippetCount": 2,
                "mergeContiguous": true,
                "fragmentSize": 150
            }
        */
        SearchQuery postQuery = new SearchQuery();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode rootNode = builder.property("cm:content", "budget").create();
        postQuery.setQuery(rootNode);
        SearchQuery.HighlightOptions options = new HighlightOptions();
        options.setSnippetCount(2);
        options.setMergeContiguous(Boolean.TRUE);
        options.setPrefix("!PREFIX!");
        options.setPostfix("!SUFFIX!");
        options.setFragmentSize(150);
        postQuery.setHighlight(options);

        SearchQueryResult actualResult = searchService.query(postQuery);
        assertEquals(expectedHighlightResult, actualResult.getHighlights());
    }
}
