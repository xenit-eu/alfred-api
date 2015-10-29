package eu.xenit.apix.alfresco42.search;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.utils.java8.Optional;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("eu.xenit.apix.search.SearchService")
@OsgiService
public class SearchServiceImpl42 extends SearchService {
    private int searchLimit;

    @Autowired
    public SearchServiceImpl42(org.alfresco.service.cmr.search.SearchService searchService, SearchFacetsService facetService,
            SearchResultCountService resultCountService, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        super(searchService, facetService, resultCountService, apixToAlfrescoConversion);
    }

    @Override
    protected void setSearchLimit(SearchParameters searchParameters, int maxItems) {
        searchLimit = maxItems;
    }

    @Override
    protected int getSearchLimit(SearchParameters searchParameters) {
        return searchLimit;
    }
}
