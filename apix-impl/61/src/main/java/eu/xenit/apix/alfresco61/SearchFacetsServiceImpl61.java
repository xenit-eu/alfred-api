package eu.xenit.apix.alfresco61;

import eu.xenit.apix.alfresco.search.SearchFacetsServiceImpl5x;
import eu.xenit.apix.translation.ITranslationService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class SearchFacetsServiceImpl61 extends SearchFacetsServiceImpl5x {

    private static final Logger logger = LoggerFactory.getLogger(SearchFacetsServiceImpl61.class);

    @Autowired
    public SearchFacetsServiceImpl61(ServiceRegistry serviceRegistry, SolrFacetService facetService,
            ITranslationService translationService) {
        super(serviceRegistry, facetService, translationService);
    }
}
