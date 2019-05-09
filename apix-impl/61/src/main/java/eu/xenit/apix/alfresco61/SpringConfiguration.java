package eu.xenit.apix.alfresco61;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.translation.ITranslationService;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SpringConfiguration {

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    SolrFacetService solrFacetService;

    @Autowired
    ApixToAlfrescoConversion apixToAlfrescoConversion;

    @Autowired
    @Qualifier("messageService")
    private MessageService messageService;

    @Autowired
    private ITranslationService translationService;

    public SearchFacetsService eu_xenit_apix_search_searchFacetsService61Apix() {
        return new SearchFacetsServiceImpl61(serviceRegistry, solrFacetService, translationService);
    }

    @Bean(name = "eu.xenit.apix.search.SearchFacetsService61")
    public SearchFacetsService searchFacetsServiceApix() {
        return new SearchFacetsServiceImpl61(serviceRegistry, solrFacetService, translationService);
    }
}
