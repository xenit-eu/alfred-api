package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQueryResult;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * An {@link eu.xenit.apix.search.ISearchService} implementation used as a spy for testing the behavior of the base
 * {@link SearchService} class
 */
public class SearchServiceBaseSpy extends SearchService {

    private int processedResults = 0;

    public SearchServiceBaseSpy(org.alfresco.service.cmr.search.SearchService searchService,
            SearchFacetsService facetService,
            ApixToAlfrescoConversion apixToAlfrescoConversion,
            PropertyService propertyService) {
        super(searchService, facetService, apixToAlfrescoConversion, propertyService);
    }

    @Override
    protected SearchQueryResult processResults(ResultSet rs, SearchQuery postQuery, SearchParameters searchParameters) {
        processedResults += rs.getNodeRefs().size();
        return super.processResults(rs, postQuery, searchParameters);
    }

    public int getProcessedResults() {
        return processedResults;
    }
}
