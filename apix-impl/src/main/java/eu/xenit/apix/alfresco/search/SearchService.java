package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQueryConsistency;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.utils.java8.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchService implements ISearchService {

    public static final int MAX_ITEMS_DEFAULT = 1000;
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    protected SearchFacetsService facetService;
    protected ApixToAlfrescoConversion c;
    protected org.alfresco.service.cmr.search.SearchService searchService;
    protected PropertyService propertyService;

    public SearchService(org.alfresco.service.cmr.search.SearchService searchService, SearchFacetsService facetService,
            ApixToAlfrescoConversion apixToAlfrescoConversion, PropertyService propertyService) {
        this.searchService = searchService;
        this.facetService = facetService;
        this.c = apixToAlfrescoConversion;
        this.propertyService = propertyService;
    }

    public org.alfresco.service.cmr.search.SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(org.alfresco.service.cmr.search.SearchService searchService) {
        this.searchService = searchService;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public String toFtsQuery(SearchQuery q) {
        FtsNodeVisitor ftsNodeVisitor = new FtsNodeVisitor(propertyService);
        return ftsNodeVisitor.visit(q.getQuery());
    }

    protected SearchParameters buildSearchParameters(SearchQuery postQuery) {
        if (postQuery.getFacets().isEnabled() && postQuery.getConsistency() == SearchQueryConsistency.TRANSACTIONAL) {
            throw new RuntimeException(
                    "Transaction consistency does not support retrieval as facets. Either use query consistency eventual or disable facets in your query.");
        } else if (postQuery.getFacets().isEnabled() && postQuery.getConsistency() == SearchQueryConsistency.TRANSACTIONAL_IF_POSSIBLE) {
            postQuery.setConsistency(SearchQueryConsistency.EVENTUAL);
        }

        SearchParameters searchParameters = new SearchParameters();

        String query = toFtsQuery(postQuery);
        int argSkipCount = postQuery.getPaging().getSkip();
        int maxItems = Optional.ofNullable(postQuery.getPaging().getLimit()).orElse(MAX_ITEMS_DEFAULT);

        // XENFRED-1516
        // Bug in Solr: "an *ArrayIndexOutOfBoundsException* occurs if _rows_ + _start_ > 2147483647"
        // see also: https://issues.apache.org/jira/browse/SOLR-3513
        // Fixed in Solr 6.4. Probably in earlier versions too, but cannot find the exact fix version.
        final int SOLR_BUG_MAX = 2147483647;
        if(argSkipCount + maxItems > SOLR_BUG_MAX) {
            maxItems = SOLR_BUG_MAX - argSkipCount;
        }

        // TODO: correctly implement skip and limit, now just manually limiting
        // 5.x will add maxItems to searchParameters
        // 4.2 will bodge it by putting maxItems in a variable for use during results parsing
        setSearchLimit(searchParameters, maxItems);

        // Skip count
        if (argSkipCount > 0) {
            searchParameters.setSkipCount(argSkipCount);
        }

        // The workspace-store we are searching
        setSearchStore(searchParameters, postQuery);

        // Select query language
        // Changed to solr because normal fts sometimes goes through the database
        searchParameters.setLanguage(org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO);
        if (postQuery.getConsistency() == SearchQueryConsistency.TRANSACTIONAL) {
            searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
            searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
        } else if (postQuery.getConsistency() == SearchQueryConsistency.TRANSACTIONAL_IF_POSSIBLE) {
            searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);
            searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
        } else if (postQuery.getConsistency() == SearchQueryConsistency.EVENTUAL) {
            searchParameters.setQueryConsistency(QueryConsistency.EVENTUAL);
        } else {
            throw new RuntimeException("Invalid query consistency: " + postQuery.getConsistency());
        }

        // Add facet support (if requested)
        facetService.addFacetSearchParameters(postQuery.getFacets(), searchParameters, query, postQuery.getQuery());

        // Set query
        searchParameters.setQuery(query);
        if (!postQuery.getOrderBy().isEmpty()) {
            List<QName> multivaluePropertiesInOrderBy = new ArrayList<>();
            for (int i = 0; i < postQuery.getOrderBy().size(); i++) {
                SearchQuery.OrderBy orderBy = postQuery.getOrderBy().get(i);
                boolean ascending = false;
                if (orderBy.getOrder().equals(SearchQuery.OrderBy.Order.ASCENDING)) {
                    ascending = true;
                }
                QName property = orderBy.getProperty();
                org.alfresco.service.namespace.QName alfProperty = c.alfresco(property);
                if (propertyService.GetPropertyDefinition(property).isMultiValued()) {
                    multivaluePropertiesInOrderBy.add(property);
                    continue;
                };
                SortDefinition sortDefinition = new SortDefinition(SortType.FIELD, "@" + alfProperty.toString(),
                        ascending);
                if (alfProperty.getNamespaceURI().isEmpty()) {
                    sortDefinition = new SortDefinition(SortType.FIELD, property.getValue(), ascending);
                }
                searchParameters.addSort(sortDefinition);
            }
            if (!multivaluePropertiesInOrderBy.isEmpty()) {
                String message = "Search ordering cannot contain multivalue properties. Searchrequest rejected because of orderby on ";
                message += multivaluePropertiesInOrderBy.stream().map(QName::toString).collect(Collectors.joining(", "));
                throw new IllegalArgumentException(message);
            }
        }

        if (postQuery.getLocale() != null) {
            searchParameters.addLocale(postQuery.getLocale());
        }
        return searchParameters;
    }

    protected void setSearchLimit(SearchParameters searchParameters, int max) {
        // MaxItems will impose a hard limit on the total number of results.
        // Setting it to -1 signifies all results should be counted in the totalCount.
        // However, this can lead to dangerously high memory usage in Solr, leading to too much GC, leading to an
        // unreactive system. See also https://lucene.apache.org/solr/guide/7_4/pagination-of-results.html
        searchParameters.setMaxItems(max);
    }

    protected int getSearchLimit(SearchParameters searchParameters) {
        return searchParameters.getMaxItems();
    }

    protected void logQuery(SearchParameters searchParameters) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Searchquery: " + searchParameters.toString()
                    .replaceAll("query=(.*), stores=", "\n\nquery=$1\n\n, stores="));
        }

        // cfr APIX-109 (second regex is very expensive in time so put it as separate trace log instead of debug)
        if (logger.isTraceEnabled()) {
            logger.trace("Executing Searchquery: " + searchParameters.toString()
                    .replaceAll("query=(.*), stores=", "\n\nquery=$1\n\n, stores=")
                    .replaceAll(
                            "((?:(?!\\s(?:OR|AND)\\s).)*\\s(?:OR|AND)\\s(?:(?!\\s(?:OR|AND)\\s).)*(?:\\s(?:OR|AND)\\s)?)",
                            "\n$1"));
        }
    }

    protected SearchQueryResult processResults(ResultSet rs, SearchQuery postQuery, SearchParameters searchParameters) {
        SearchQueryResult results = new SearchQueryResult();

        int count = 0;
        int limit = getSearchLimit(searchParameters);
        for (ResultSetRow row : rs) {
            results.addResult(c.apix(row.getNodeRef()));
            ++count;
            // limit < 0 means unlimited
            if (limit > 0 && count >= limit) {
                // TODO: correctly implement skip and limit, now just manually limiting
                break;
            }
        }
        results.setTotalResultCount(rs.getNumberFound());

        // Store facet results in output-model (if present)
        List<FacetSearchResult> facetResults = facetService
                .getFacetResults(postQuery.getFacets(), rs, searchParameters);
        results.setFacets(facetResults);

        return results;
    }

    // Originally from Alfred Desktop search webscript
    public SearchQueryResult query(SearchQuery postQuery) {
        ResultSet rs = null;
        try {
            SearchParameters searchParameters = buildSearchParameters(postQuery);
            logQuery(searchParameters);

            // Execute query
            long start = System.currentTimeMillis();
            rs = searchService.query(searchParameters);
            long duration = System.currentTimeMillis() - start;

            logger.debug("Query took " + duration + " ms");

            start = System.currentTimeMillis();
            // Process results
            SearchQueryResult results = processResults(rs, postQuery, searchParameters);

            duration = System.currentTimeMillis() - start;
            logger.debug("Fetch results took " + duration + " ms");
            return results;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected void setSearchStore(SearchParameters searchParameters, SearchQuery postQuery) {
        eu.xenit.apix.data.StoreRef queryWorkspace = postQuery.getWorkspace();
        StoreRef store = queryWorkspace != null
                ? c.alfresco(queryWorkspace)
                : StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        searchParameters.addStore(store);
    }
}
