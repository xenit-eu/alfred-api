package eu.xenit.apix.alfresco60;

import eu.xenit.apix.alfresco.search.SearchFacetsServiceImpl;
import eu.xenit.apix.translation.ITranslationService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class SearchFacetsServiceImpl60 extends SearchFacetsServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(SearchFacetsServiceImpl60.class);

    @Autowired
    public SearchFacetsServiceImpl60(ServiceRegistry serviceRegistry, SolrFacetService facetService,
            ITranslationService translationService) {
        super(serviceRegistry, facetService, translationService);
    }
}
