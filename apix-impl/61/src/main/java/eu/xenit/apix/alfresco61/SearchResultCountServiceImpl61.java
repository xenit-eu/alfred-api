package eu.xenit.apix.alfresco61;

import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.search.SearchQuery;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;


public class SearchResultCountServiceImpl61 implements SearchResultCountService {

    public SearchResultCountServiceImpl61() {
    }

    @Override
    public long countResults(SearchQuery.PagingOptions pagingOptions, ResultSet rs, SearchParameters sp) {
        return rs.getNumberFound();
    }
}
