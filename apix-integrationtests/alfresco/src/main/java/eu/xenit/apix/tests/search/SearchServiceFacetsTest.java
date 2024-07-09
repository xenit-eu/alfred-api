package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertTrue;

import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.FacetOptions;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.server.ApplicationContextProvider;
import eu.xenit.apix.util.SolrTestHelperImpl;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * NOTICE:
 *
 * This class contains tests with facets that were only supported from Alfresco 5+.
 */
public class SearchServiceFacetsTest extends SearchServiceTest {

    private static final String ADMIN_USER_NAME = "admin";
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceFacetsTest.class);

    private ApplicationContext testApplicationContext;
    private SolrFacetService facetService;
    protected SolrTestHelperImpl solrHelper;
    protected ISearchService searchService;

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        // initialiseBeans SearchServiceTest
        SetupSearchServiceTest();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        facetService = testApplicationContext.getBean(SolrFacetService.class);
        solrHelper = testApplicationContext.getBean(SolrTestHelperImpl.class);
        searchService = testApplicationContext.getBean(ISearchService.class);
    }

    @Test
    public void TestGetWithFacetsIncludesCustomFilterFacets() throws InterruptedException {
        solrHelper.waitForTransactionSync();
        // Add a new facet filter
        String newFacetFilterId = "test_filter";
        // Note that facets with 0 hits are not returned.
        // Also note that adding new property would force us to wait with searching until Solr has reindexed.
        // Therefore we create a filter facet based on a property of which we are sure already exists on docs.
        QName facetQName = ContentModel.PROP_LOCALE;

        SolrFacetProperties newFacet = new SolrFacetProperties.Builder()
                .filterID(newFacetFilterId)
                .facetQName(facetQName)
                .displayName("Wot if me nan woz fat")
                .displayControl("Simple Filter")
                .maxFilters(10)
                .hitThreshold(1)
                .minFilterValueLength(1)
                .sortBy("ASCENDING")
                .scope("ALL")
                .isEnabled(true)
                .isDefault(true)
                .build();
        logger.error("SolrFacetProperties newFacet = {}", newFacet);
        facetService.createFacetNode(newFacet);

        try {
            // Perform search query
            SearchSyntaxNode node = new QueryBuilder().term("type", "cm:content").create();

            SearchQuery.FacetOptions options = new SearchQuery.FacetOptions();
            options.setEnabled(true);

            SearchQuery query = new SearchQuery();
            query.setQuery(node);
            query.setFacets(options);

            SearchQueryResult result = searchService.query(query);

            // Results must contain newly created facet filter
            boolean isFacetFound = false;
            for (FacetSearchResult facet : result.getFacets()) {
                if (QName.createQName(facet.getName()).equals(facetQName)) {
                    isFacetFound = true;
                }
            }
            assertTrue("'" + facetQName + "' not found in returned facets.", isFacetFound);
        } finally {
            // TODO - Breaks the test, but not needed aslong as we run it once.
//            facetService.deleteFacet(newFacetFilterId);
        }
    }

    @Test
    public void TestGetBucketedFacets() throws InterruptedException {
        solrHelper.waitForTransactionSync();
        // Query that should return default facets
        // There are 6 default facets: mimetype, modifier, creator, created, modified and size
        // These last 3 are bucketed facets, so we check that they're included
        Set<String> bucketedFacetNames = new HashSet<>(Arrays.asList(
                ContentModel.PROP_CREATED.toString(),
                ContentModel.PROP_MODIFIED.toString(),
                ContentModel.PROP_CONTENT.toString() + ".size"));
        Set<String> foundBucketedFacets = new HashSet<>();
        SearchSyntaxNode node = new QueryBuilder().term("type", "cm:content").create();

        SearchQuery.FacetOptions options = new FacetOptions();
        options.setEnabled(true);

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        query.setFacets(options);

        SearchQueryResult result = searchService.query(query);
        // Search in results (Because of alf 4.2 source language level is 1.7. No lambdas to make this pretty)
        for (FacetSearchResult facetResult : result.getFacets()) {
            String facetName = facetResult.getName();
            if (bucketedFacetNames.contains(facetName)) {
                // This facet is a bucketed one, add it to the "found" set
                foundBucketedFacets.add(facetName);
            }
        }
        // At this point foundBucketedFacets should have become equal to bucketedFacetNames
        Assert.assertEquals(bucketedFacetNames, foundBucketedFacets);
    }
}
