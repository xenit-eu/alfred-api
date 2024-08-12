package eu.xenit.alfred.api.tests.search;

import static org.junit.Assert.assertEquals;

import eu.xenit.alfred.api.alfresco.search.SearchService;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.search.Highlights.HighlightResult;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQuery.HighlightOptions;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import eu.xenit.alfred.api.util.SolrTestHelperImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Disabled for GHA build")
public class TermHitHighlightingTest extends JavaApiBaseTest {

    INodeService nodeService;
    SearchService searchService;
    RetryingTransactionHelper retryingTransactionHelper;
    private final SolrTestHelperImpl solrHelper;
    private static final String FURIES_TXT =
            "The furies are at home\nin the mirror; it is their address.\nEven the clearest water,\nif deep enough can drown.\n"
                    + "\nNever think to surprise them.\nYour face approaching ever\nso friendly is the white flag\nthey ignore. There is no truce\n"
                    + "\nwith the furies. A mirror’s temperature is always at zero.\n";

    public TermHitHighlightingTest() {
        // initialise the local beans
        nodeService = getBean(INodeService.class);
        retryingTransactionHelper = getBean(RetryingTransactionHelper.class);
        solrHelper = getBean(SolrTestHelperImpl.class);
    }

    @Before
    public void createHighlightTestTxt() throws InterruptedException {
        super.cleanUp();
        retryingTransactionHelper.doInTransaction(() -> {
            File txtFile = super.createTextFileWithContent("Furies.txt", FURIES_TXT);
            FileInfo mainTestFolder = super.createMainTestFolder(getNodeAtPath("/app:company_home"));
            InputStream inputStream = new FileInputStream(txtFile);
            NodeRef target = nodeService.createNode(
                    super.c.apix(mainTestFolder.getNodeRef()), txtFile.getName(),
                    super.c.apix(ContentModel.TYPE_CONTENT));
            nodeService.setContent(target, inputStream, txtFile.getName());
            return null;
        }, false, true);
        solrHelper.waitForTransactionSync();
        solrHelper.waitForContentSync();
    }

    @After
    public void cleanupAfterHighlightTest() {
        super.cleanUp();
    }

    @Test
    /** Test all major parameters for term hit highlighting */
    public void searchResponseContainsHighlights() throws InterruptedException {
        int initialCleanDocs = solrHelper.getNumberOfFtsStatusCleanDocs();
        List<HighlightResult> expected = List.of(new HighlightResult("cm:content", List.of(
                "The !PREFIX!furies!SUFFIX! are at home\nin the mirror; it is their address.\n"
                        + "Even the clearest water,\nif deep enough can drown.\n\n"
                        + "Never think to surprise them.\nYour face approaching ever\n"
                        + "so friendly is the white flag\nthey ignore. There is no truce\n\n"
                        + "with the !PREFIX!furies!SUFFIX!. A mirror’s temperature is always at zero.\n\n")));

        // Waiting for Solr's indexing process to catch up before executing test.
        solrHelper.waitForTransactionSync();
        // Solr txn(metadata) and content indexing are separate.
        // There is no good way to track the total # of nodes for which content needs to be indexed.
        // The best we can do is check if there are nodes detected that still need to be indexed.
        // therefore we wait a period for solr to detect the nodes,
        // then use the check to wait until the indexing process is finished.
        SearchServiceTest.waitAWhile(20);
        solrHelper.waitForContentSync(initialCleanDocs);

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

        Map<String, List<HighlightResult>> actualResult = searchService.query(postQuery).getHighlights().getNoderefs();

        // Splitting asserts into check on length and check on highlight content due to differing nodeId
        // for expected and actual.
        // We only expect this one result due to controlled input.
        assertEquals(1, actualResult.size());
        // Given we expect only 1 entry, we can blindly pluck it from the map in the highlightResult
        assertEquals(expected, actualResult.entrySet().iterator().next().getValue());
    }
}
