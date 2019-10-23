package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQueryConsistency;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.util.SolrTestHelper;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

abstract public class SearchServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(SearchServiceTest.class);
    private static final String ADMIN_USER_NAME = "admin";
    @Autowired
    ISearchService searchService;
    @Autowired
    NodeService nodeService;
    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    TransactionService transactionService;
    @Autowired
    Repository repository;
    @Autowired
    DataSource dataSource;
    @Autowired
    @Qualifier("Search")
    SwitchableApplicationContextFactory searchSubSystem;

    protected SolrTestHelper solrTestHelper;

    final protected static String APIXTEST_NS = "http://test.apix.xenit.eu/model/content";
    final protected static QName APIXTEST_LANGUAGE = QName.createQName(APIXTEST_NS, "language");
    final protected static QName APIXTEST_DOCUMENT_STATUS = QName.createQName(APIXTEST_NS, "documentStatus");

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        solrTestHelper = new SolrTestHelper("/solr4", dataSource, searchSubSystem);
    }

    @After
    public void tearDown() {
        cleanUp();
    }

    @Test
    public void TestGetWithoutFacets() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);

        SearchQueryResult result = searchService.query(query);

        assertTrue(result.getNoderefs().size() > 0);
        Assert.assertEquals(null, result.getFacets());
    }

    @Test
    public void TestGetWithFacets() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
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
    public void TestLimit() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setLimit(3);
        SearchQueryResult result = searchService.query(query);

        Assert.assertEquals(3, result.getNoderefs().size());
    }

    @Test
    public void TestSkip() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
        QueryBuilder builder = new QueryBuilder();
        eu.xenit.apix.search.nodes.SearchSyntaxNode node = builder.term("type", "cm:folder").create();

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
    public void TestTotalCount() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.getPaging().setLimit(2);
        SearchQueryResult result = searchService.query(query);

        logger.debug("Total: " + result.getTotalResultCount());
        logger.debug("Returned: " + result.getNoderefs().size());
        assertTrue(result.getTotalResultCount() > result.getNoderefs().size());
    }

    @Test
    public void testPropertyRange() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
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

    @Test
    public void TestTotalCountSkip() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder.term("type", "cm:folder").create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);

        query.getPaging().setLimit(2);
        SearchQueryResult result = searchService.query(query);

        query.getPaging().setSkip(10);

        SearchQueryResult resultSkip = searchService.query(query);

        Assert.assertEquals(resultSkip.getTotalResultCount(), result.getTotalResultCount());
        Assert.assertEquals(2, resultSkip.getNoderefs().size());
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
    public void TestQueryConsistency_Transactional() throws InterruptedException {
        solrTestHelper.waitForSolrSync();
        final String theTitle =
                "The title to search for in SearchService.TestQueryConsistency_Transactional" + System.nanoTime();

        final NodeRef theNewNode = transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                    @Override
                    public NodeRef execute() throws Throwable {
                        cleanUp();
                        NodeRef companyHomeRef = repository.getCompanyHome();
                        FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);

                        // create node
                        ChildAssociationRef testFolderAssoc = serviceRegistry.getNodeService()
                                .createNode(mainTestFolder.getNodeRef(), ContentModel.ASSOC_CONTAINS,
                                        org.alfresco.service.namespace.QName
                                                .createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "aNode.txt"),
                                        ContentModel.TYPE_CONTENT);
                        NodeRef theNewNode = testFolderAssoc.getChildRef();

                        Map<org.alfresco.service.namespace.QName, Serializable> props = serviceRegistry
                                .getNodeService()
                                .getProperties(theNewNode);
                        props.put(ContentModel.PROP_TITLE, theTitle);
                        serviceRegistry.getNodeService().setProperties(theNewNode, props);
                        return theNewNode;
                    }
                }, false, true);

        // Don't wait, immediately do a search

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
                    @Override
                    public Boolean execute() throws Throwable {
                        QueryBuilder builder = new QueryBuilder();
                        eu.xenit.apix.search.nodes.SearchSyntaxNode node = builder
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
                    }
                }, false, true);
    }

    @Test
    public void TestExactMatchProperty() throws IOException, InterruptedException {
        solrTestHelper.waitForSolrSync();
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

    private void withLocale(Locale locale, AuthenticationUtil.RunAsWork<Object> work) throws Exception {
        Locale prevLocale = I18NUtil.getLocale();
        I18NUtil.setLocale(locale);

        try {
            work.doWork();
        } finally {
            I18NUtil.setLocale(prevLocale);
        }
    }

    /**
     * Implemented in alfresco version specific subclasses to set up and tear down solr facets.
     * @param work
     * @throws Exception
     */
    abstract protected void withTestFacets(AuthenticationUtil.RunAsWork<Object> work) throws Exception;

}
