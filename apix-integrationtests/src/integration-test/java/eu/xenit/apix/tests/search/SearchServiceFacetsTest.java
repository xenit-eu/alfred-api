package eu.xenit.apix.tests.search;

import static org.junit.Assert.assertTrue;

import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.FacetOptions;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.util.SolrTestHelper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * NOTICE:
 *
 * This class contains tests with facets that were only supported from Alfresco 5+.
 */
public class SearchServiceFacetsTest extends SearchServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(SearchServiceFacetsTest.class);
    private static final String ADMIN_USER_NAME = "admin";

    @Autowired
    private SolrFacetService facetService;

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        String subsystem = properties.getProperty("index.subsystem.name");
        String solrBaseUrl = subsystem.equals("solr4") ? "/solr4" : "/solr";
        solrTestHelper = new SolrTestHelper(solrBaseUrl, dataSource, searchSubSystem);
    }

    @Override
    protected void withTestFacets(final RunAsWork<Object> work) throws Exception {
        SolrFacetProperties languageFacet = new SolrFacetProperties.Builder()
                .filterID("document_language")
                .facetQName(APIXTEST_LANGUAGE)
                .displayControl("Simple Filter")
                .displayName("Language")
                .maxFilters(10)
                .hitThreshold(1)
                .minFilterValueLength(1)
                .sortBy("ASCENDING")
                .scope("ALL")
                .isEnabled(true)
                .isDefault(true)
                .build();

        final SolrFacetProperties documentStatusFacet = new SolrFacetProperties.Builder()
                .filterID("document_status")
                .facetQName(APIXTEST_DOCUMENT_STATUS)
                .displayControl("Simple Filter")
                .displayName("Status")
                .maxFilters(10)
                .hitThreshold(1)
                .minFilterValueLength(1)
                .sortBy("ASCENDING")
                .scope("ALL")
                .isEnabled(true)
                .isDefault(true)
                .build();

        withFacet(languageFacet, new RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                withFacet(documentStatusFacet, work);
                return null;
            }
        });

    }
    private void withFacet(SolrFacetProperties facetProperties, AuthenticationUtil.RunAsWork<Object> work) throws Exception {
        facetService.createFacetNode(facetProperties);
        try {
            work.doWork();
        } finally {
            facetService.deleteFacet(facetProperties.getFilterID());
        }
    }

    @Test
    public void TestGetWithFacetsIncludesCustomFilterFacets() throws InterruptedException {
        solrTestHelper.waitForSolrSync();
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
            facetService.deleteFacet(newFacetFilterId);
        }
    }

    @Test
    public void TestGetBucketedFacets() throws InterruptedException {
        solrTestHelper.waitForSolrSync();
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
        // Search in results (Because of alf 4.2 source language level is 1.7. No lambdas to make this pretty 🙁)
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
