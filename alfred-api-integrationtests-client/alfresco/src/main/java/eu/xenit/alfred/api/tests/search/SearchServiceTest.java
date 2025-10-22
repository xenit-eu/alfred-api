package eu.xenit.alfred.api.tests.search;

import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQueryConsistency;
import eu.xenit.alfred.api.search.SearchQueryResult;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import eu.xenit.alfred.api.util.SolrTestHelperImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SearchServiceTest extends JavaApiBaseTest {

    private static final String LONG_MAX_VALUE = String.valueOf(Long.MAX_VALUE);
    private static final String LONG_MAX_VALUE_PLUS_ONE = new BigInteger(LONG_MAX_VALUE).add(new BigInteger("1")).toString();

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceTest.class);
    private static final String ADMIN_USER_NAME = "admin";
    public static final String DESCRIPTION_SET_OF_1001 = "descriptionSetOf1001";
    protected ISearchService searchService;
    protected NodeService nodeService;
    protected NamespacePrefixResolver namespacePrefixResolver;
    protected SolrTestHelperImpl solrHelper;

    public SearchServiceTest() {
        searchService = getBean(ISearchService.class);
        nodeService = serviceRegistry.getNodeService();
        namespacePrefixResolver = getBean("namespaceService", NamespacePrefixResolver.class);
        solrHelper = getBean(SolrTestHelperImpl.class);
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
    }

    @After
    public void tearDown() {
        cleanUp();
    }

    @Test
    public void TestGetWithoutFacets() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);

        SearchQueryResult result = searchService.query(query);

        assertTrue(result.getNoderefs().size() > 0);
        Assert.assertNull(result.getFacets());
    }

    @Test
    public void TestGetWithFacets() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:content").create();

        SearchQuery.FacetOptions opts = new SearchQuery.FacetOptions();
        opts.setEnabled(true);

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.setFacets(opts);

        SearchQueryResult result = searchService.query(query);

        assertTrue(result.getNoderefs().size() > 0);
        assertTrue(result.getFacets().size() > 0);

        for (FacetSearchResult f : result.getFacets()) {
            logger.debug(f.toString());
        }
    }

    @Test
    public void TestLimit() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setLimit(3);
        SearchQueryResult result = searchService.query(query);

        Assert.assertEquals(3, result.getNoderefs().size());
    }

    @Test
    public void TestSkip() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setLimit(3);
        SearchQueryResult resultAll = searchService.query(query);

        query.getPaging().setSkip(1);
        query.getPaging().setLimit(2);
        SearchQueryResult result = searchService.query(query);

        Assert.assertEquals(3, resultAll.getNoderefs().size());
        Assert.assertEquals(2, result.getNoderefs().size());

        Assert.assertEquals(resultAll.getNoderefs().get(1), result.getNoderefs().get(0));
        Assert.assertEquals(resultAll.getNoderefs().get(2), result.getNoderefs().get(1));
    }

    @Test
    public void TestTotalCount() throws InterruptedException {
        transactionService.getRetryingTransactionHelper()
                .doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {

                    NodeRef companyHomeRef = repository.getCompanyHome();

                    FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);
                    FileInfo testFolder = createTestFolder(mainTestFolder.getNodeRef(), "testFolder");
                    FileInfo testNode = createTestNode(testFolder.getNodeRef(), "testNode");
                    FileInfo testNode2 = createTestNode(testFolder.getNodeRef(), "testNode2");
                    return null;
                }, false, true);

        solrHelper.waitForTransactionSync();
        // solrTestHelper has a bug. TODO ticket ALFREDAPI-425
        Thread.sleep(15000);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
                    QueryBuilder builder = new QueryBuilder();
                    SearchSyntaxNode node = builder.property(
                                    ContentModel.PROP_NAME.toPrefixString(namespacePrefixResolver),
                                    "testNode",
                                    false)
                            .create();

                    SearchQuery query = new SearchQuery();
                    query.setQuery(node);
                    SearchQueryResult result = searchService.query(query);

                    logger.debug("Total: " + result.getTotalResultCount());
                    assertEquals(2, result.getTotalResultCount());
                    return null;
                }, false, true);
    }

    private void create1001TestDocs() throws InterruptedException {
        transactionService.getRetryingTransactionHelper()
                .doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
                    NodeRef companyHomeRef = repository.getCompanyHome();

                    FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);
                    FileInfo testFolder = createTestFolder(mainTestFolder.getNodeRef(), "testFolderSetOf1001");
                    Map<QName, Serializable> props = new HashMap<>();
                    props.put(
                            QName.createQName(ALFRED_API_TESTCM_NAMESPACE,
                                    ALFRED_API_TESTCM_PROP_SEARCHSERVICELIMITTEST_SHORTNAME),
                            DESCRIPTION_SET_OF_1001
                    );
                    for (int i = 0; i < 1001; i++) {
                        FileInfo testNode = createTestNode(testFolder.getNodeRef(), "testNode-1001-" + i);
                        nodeService.addProperties(testNode.getNodeRef(), props);
                    }
                    return null;
                }, false, true);

        solrHelper.waitForTransactionSync();
        // solrTestHelper has a bug. TODO ticket ALFREDAPI-425
        Thread.sleep(15000);
    }

    @Test
    public void TestLimitedByMaxPermissionChecks_transactional() throws InterruptedException {
        create1001TestDocs();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(
                        ALFRED_API_TESTCM_PROP_SEARCHSERVICELIMITTEST_PREFIXED,
                        DESCRIPTION_SET_OF_1001,
                        true)
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setSkip(800);
        query.getPaging().setLimit(400);
        query.setConsistency(SearchQueryConsistency.TRANSACTIONAL);
        SearchQueryResult result = searchService.query(query);
        assertEquals(201, result.getNoderefs().size());
    }

    @Test
    public void TestLimitedByMaxPermissionChecks_transactional_if_possible() throws InterruptedException {
        create1001TestDocs();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(
                        ALFRED_API_TESTCM_PROP_SEARCHSERVICELIMITTEST_PREFIXED,
                        DESCRIPTION_SET_OF_1001,
                        true)
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setSkip(800);
        query.getPaging().setLimit(400);
        query.setConsistency(SearchQueryConsistency.TRANSACTIONAL_IF_POSSIBLE);
        SearchQueryResult result = searchService.query(query);
        assertEquals(201, result.getNoderefs().size());
    }

    @Test
    public void TestLimitedByMaxPermissionChecks_eventual() throws InterruptedException {
        create1001TestDocs();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(
                        ALFRED_API_TESTCM_PROP_SEARCHSERVICELIMITTEST_PREFIXED,
                        DESCRIPTION_SET_OF_1001,
                        //cant do exact searches against solr on custom props.
                        false)
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setSkip(800);
        query.getPaging().setLimit(400);
        query.setConsistency(SearchQueryConsistency.EVENTUAL);
        SearchQueryResult result = searchService.query(query);
        assertEquals(201, result.getNoderefs().size());
    }

    @Test
    public void testPropertyRange() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property("cm:created", "2010-01-01T00:00:00", "2015-01-01T00:00:00").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setLimit(2);
        SearchQueryResult result = searchService.query(query);

        logger.debug("Total: " + result.getTotalResultCount());
        assertTrue(result.getTotalResultCount() > 0);
    }

    public static void waitAWhile(int nbSeconds) throws InterruptedException {
        for (int i = 0; i < nbSeconds * 10; i++) {
            // This println is here to send data over the wire while waiting. This prevents any http proxy from closing
            //   the connection due to timeouts
            System.out.println("Waiting for solr " + i);
            Thread.sleep(100);
        }
    }

    /**
     * Not a very advanced test. This adds a new node and tries to search it immediately using transactional, which
     * should always work.
     */
    @Test
    public void TestQueryConsistency_Transactional() {
        final String theTitle =
                "The title to search for in SearchService.TestQueryConsistency_Transactional" + System.nanoTime();

        final NodeRef theNewNode = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    cleanUp();
                    NodeRef companyHomeRef = repository.getCompanyHome();
                    FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);

                    // create node
                    ChildAssociationRef testFolderAssoc = serviceRegistry.getNodeService()
                            .createNode(mainTestFolder.getNodeRef(), ContentModel.ASSOC_CONTAINS,
                                    QName
                                            .createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "aNode.txt"),
                                    ContentModel.TYPE_CONTENT);
                    NodeRef theNewNode1 = testFolderAssoc.getChildRef();

                    Map<QName, Serializable> props = serviceRegistry
                            .getNodeService()
                            .getProperties(theNewNode1);
                    props.put(ContentModel.PROP_TITLE, theTitle);
                    serviceRegistry.getNodeService().setProperties(theNewNode1, props);
                    return theNewNode1;
                }, false, true);

        // Don't wait, immediately do a search

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    QueryBuilder builder = new QueryBuilder();
                    SearchSyntaxNode node = builder
                            .property("cm:title", theTitle, true)
                            .create(); // Exact match is required
                    SearchQuery query = new SearchQuery();
                    query.setQuery(node);
                    query.setConsistency(SearchQueryConsistency.TRANSACTIONAL);
                    SearchQueryResult result = searchService.query(query);

                    assertTrue(
                            "Should find back the new node immediately when using transactional consistency!",
                            result.getNoderefs().contains(theNewNode.toString()));

                    return Boolean.TRUE;
                }, false, true);
    }

    @Test
    public void TestExactMatchProperty() {
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.property("cm:name", "Company Home", true).create(); // Exact match

        SearchQuery.FacetOptions opts = new SearchQuery.FacetOptions();
        opts.setEnabled(false);

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.setFacets(opts);

        SearchQueryResult result = searchService.query(query);

        assertTrue(result.getNoderefs().size() > 0);
        System.out.println("Comp: " + repository.getCompanyHome().getId());
        for (String f : result.getNoderefs()) {
            System.out.println(f);
        }
        assertEquals("Should have found company home node", repository.getCompanyHome().toString(),
                result.getNoderefs().get(0));
    }


    @Test
    public void validLong_onlyNode_doesNotThrowException() {
        SearchSyntaxNode node =
                new QueryBuilder()
                        .startOr()
                        .property("{http://www.alfresco.org/model/system/1.0}node-dbid", LONG_MAX_VALUE)
                        .end()
                        .create();
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(node);
        searchService.query(searchQuery);
    }

    @Test
    public void invalidLong_onlyNode_doesNotThrowException() {
        SearchSyntaxNode node =
                new QueryBuilder()
                        .startOr()
                        .property("{http://www.alfresco.org/model/system/1.0}node-dbid", LONG_MAX_VALUE_PLUS_ONE)
                        .end()
                        .create();
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(node);
        searchService.query(searchQuery);
    }

    @Test
    public void testSearchForQuotationMarkProperty() {
        final String testString = "For testing \"quotes\".";

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            NodeRef companyHomeRef = repository.getCompanyHome();

            FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);
            FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testNode");
            nodeService.setProperty(testNode.getNodeRef(), ContentModel.PROP_DESCRIPTION, testString);
            return null;
        }, false, true);

        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.property(
                        ContentModel.PROP_DESCRIPTION.toPrefixString(namespacePrefixResolver), testString, true)
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        // Set consistency to transactional so we don't have to wait for Solr
        query.setConsistency(SearchQueryConsistency.TRANSACTIONAL);
        SearchQueryResult result = searchService.query(query);

        Assert.assertEquals(1, result.getTotalResultCount());
    }
}
