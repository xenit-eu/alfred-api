package eu.xenit.apix.alfresco52;

import eu.xenit.apix.alfresco.search.SearchFacetsServiceImpl5x;
import eu.xenit.apix.translation.ITranslationService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by mhgam on 29/06/2016.
 */
public class SearchFacetsServiceImpl52 extends SearchFacetsServiceImpl5x {

    private static final Logger logger = LoggerFactory.getLogger(SearchFacetsServiceImpl52.class);

    @Autowired
    public SearchFacetsServiceImpl52(ServiceRegistry serviceRegistry, SolrFacetService facetService,
            ITranslationService translationService) {
        super(serviceRegistry, facetService, translationService);
    }
}
