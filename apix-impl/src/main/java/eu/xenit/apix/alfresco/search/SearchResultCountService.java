package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.SearchQuery;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Created by stan on 6/30/16.
 */
public interface SearchResultCountService {

    long countResults(SearchQuery.PagingOptions pagingOptions, ResultSet rs, SearchParameters sp);
}
