package eu.xenit.alfred.api.tests.search;

import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQuery.FacetOptions;
import eu.xenit.alfred.api.search.SearchQueryResult;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTICE:
 *
 * This class contains tests with facets that were only supported from Alfresco 5+.
 */
public class SearchServiceFacetsTest extends SearchServiceTest {

    private static final String ADMIN_USER_NAME = "admin";
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceFacetsTest.class);


    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
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
        logger.debug("SolrFacetProperties newFacet = {}", newFacet);
        facetService.createFacetNode(newFacet);

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
                ContentModel.PROP_CONTENT + ".size"));
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
