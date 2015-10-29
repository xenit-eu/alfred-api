package eu.xenit.apix.alfresco51;

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
public class SearchServiceImpl51 extends SearchService {

    @Autowired
    public SearchServiceImpl51(org.alfresco.service.cmr.search.SearchService searchService,
            SearchFacetsService facetService,
            SearchResultCountService resultCountService, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        super(searchService, facetService, resultCountService, apixToAlfrescoConversion);
    }
}
