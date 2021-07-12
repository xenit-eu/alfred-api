package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.search.Highlights;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.HighlightOptions;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.tests.BaseTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.tika.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TermHitHighlightingTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(TermHitHighlightingTest.class);

    @Autowired
    SearchService searchService;
    @Autowired
    RetryingTransactionHelper retryingTransactionHelper;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void createHighlightTestDocument() throws InterruptedException {
        super.cleanUp();
        retryingTransactionHelper.doInTransaction(() -> {
            NodeService nodeService = super.serviceRegistry.getNodeService();
            FileInfo mainTestFolder = super.createMainTestFolder(getNodeAtPath("/app:company_home"));
            FileInfo targetNodeFileInfo = super.createTestNode(mainTestFolder.getNodeRef(), "furies.txt");
            NodeRef targetNode = targetNodeFileInfo.getNodeRef();
            ContentWriter writer = super.serviceRegistry.getContentService()
                    .getWriter(targetNode, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/pdf");
            InputStream resourceStream = getClass().getClassLoader().getResource(
                    "highlightTest/Furies.pdf").openStream();
            try {
                writer.putContent(resourceStream);
            } finally {
                IOUtils.closeQuietly(resourceStream);
            }
            ContentData contentData = (ContentData) nodeService.getProperty(
                    targetNode, ContentModel.PROP_CONTENT);
            nodeService.setProperty(targetNode, ContentModel.PROP_CONTENT, contentData);
            return null;
        }, false, true);
        solrHelper.waitForSolrSync();
    }

    @After
    public void cleanupAfterHighlightTest() {
        super.cleanUp();
    }

    @Test
    /** Test all major parameters for term hit highlighting */
    public void searchResponseContainsHighlights() throws IOException, InterruptedException {
        // Waiting for Solr's indexing process to catch up before executing test.
        solrHelper.waitForSolrSync();
        SearchServiceTest.waitAWhile(20);

        URL expectedHighlightsJson = getClass().getClassLoader().getResource(
                "highlightTest/searchRequestWithHighlighting_withAllMajorCustomParams.json");
        Highlights expectedHighlightResult = mapper.readValue(expectedHighlightsJson, Highlights.class);

        /* The options set are equivalent to the following JSON:
            "query": {
                "property": {
                    "exact": false,
                    "name": "cm:content",
                    "value": "furies"
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
        SearchSyntaxNode rootNode = builder.property("cm:content", "furies").create();
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
