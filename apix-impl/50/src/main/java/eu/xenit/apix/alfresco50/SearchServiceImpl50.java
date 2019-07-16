package eu.xenit.apix.alfresco50;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("eu.xenit.apix.search.SearchService")
@OsgiService
public class SearchServiceImpl50 extends SearchService {

    @Autowired
    public SearchServiceImpl50(
            org.alfresco.service.cmr.search.SearchService searchService,
            SearchFacetsService facetService,
            ApixToAlfrescoConversion apixToAlfrescoConversion, PropertyService propertyService) {
        super(searchService, facetService, apixToAlfrescoConversion, propertyService);
    }
}
