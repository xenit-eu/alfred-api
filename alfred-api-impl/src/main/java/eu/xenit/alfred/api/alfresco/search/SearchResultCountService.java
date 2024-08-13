package eu.xenit.alfred.api.alfresco.search;

import eu.xenit.alfred.api.search.SearchQuery;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Created by stan on 6/30/16.
 */
public interface SearchResultCountService {

    long countResults(SearchQuery.PagingOptions pagingOptions, ResultSet rs, SearchParameters sp);
}
