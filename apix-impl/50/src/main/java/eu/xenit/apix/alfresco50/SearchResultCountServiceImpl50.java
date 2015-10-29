package eu.xenit.apix.alfresco50;

import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.search.SearchQuery;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Created by stan on 6/30/16.
 */
public class SearchResultCountServiceImpl50 implements SearchResultCountService {

    public SearchResultCountServiceImpl50() {
        // ioc
    }

    @Override
    public long countResults(SearchQuery.PagingOptions pagingOptions, ResultSet rs, SearchParameters sp) {
        return rs.getNumberFound();
    }
}
