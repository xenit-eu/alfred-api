package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.search.Highlights;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.HighlightOptions;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.tests.BaseTest;
import java.io.File;
import java.io.FileInputStream;
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
    INodeService nodeService;
    @Autowired
    SearchService searchService;
    @Autowired
    RetryingTransactionHelper retryingTransactionHelper;

    private ObjectMapper mapper = new ObjectMapper();
    private static final String FURIES_TXT =
            "\nThe furies are at home\nin the mirror; it is their address.\nEven the clearest water,\nif deep enough can drown.\n"
            + "\nNever think to surprise them.\nYour face approaching ever\nso friendly is the white flag\nthey ignore. There is no truce\n"
            + "\nwith the furies. A mirror’s temperature\nis always at zero. It is ice\nin the veins. Its camera\nis an X-ray. It is a chalice\n"
            + "\nheld out to you in\nsilent communion, where gaspingly\nyou partake of a shifting\nidentity never your own.";

    @Before
    public void createHighlightTestTXT() throws InterruptedException {
        super.cleanUp();
        retryingTransactionHelper.doInTransaction(() -> {
            File txtFile = super.createTextFileWithContent("Furies.txt", FURIES_TXT);
            FileInfo mainTestFolder = super.createMainTestFolder(getNodeAtPath("/app:company_home"));
            InputStream inputStream = new FileInputStream(txtFile);
            eu.xenit.apix.data.NodeRef target = nodeService.createNode(super.c.apix(mainTestFolder.getNodeRef()), txtFile.getName(),
                    super.c.apix(ContentModel.TYPE_CONTENT));
            nodeService.setContent(target, inputStream, txtFile.getName());
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
        HighlightOptions options = new HighlightOptions();
        options.setSnippetCount(2);
        options.setMergeContiguous(Boolean.TRUE);
        options.setPrefix("!PREFIX!");
        options.setPostfix("!SUFFIX!");
        options.setFragmentSize(150);
        postQuery.setHighlight(options);

        SearchQueryResult actualResult = searchService.query(postQuery);
        // Splitting asserts into check on length and check on highlight content due to differing nodeId for expected
        // and actual.
        // We only expect this one result due to controlled input.
        assertEquals(1, actualResult.getHighlights().getNoderefs().size());
        // Given we expect only 1 entry, we can blindly pluck it from the map in the highlightResult
        assertEquals(expectedHighlightResult.getNoderefs().entrySet().iterator().next().getValue(),
                actualResult.getHighlights().getNoderefs().entrySet().iterator().next().getValue());
    }
}
