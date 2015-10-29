package eu.xenit.apix.alfresco42;

import com.github.dynamicextensionsalfresco.annotations.AlfrescoService;
import com.github.dynamicextensionsalfresco.annotations.ServiceType;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.categories.CategoryService;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.alfresco42.search.SearchFacetsServiceImpl42;
import eu.xenit.apix.alfresco42.search.SearchResultCountServiceImpl42;
import eu.xenit.apix.alfresco42.search.configuration.FacetConfiguration;
import eu.xenit.apix.alfresco42.search.solr.SolrClient;
import eu.xenit.apix.categories.ICategoryService;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Stan on 15-Feb-16.
 */

@Configuration
public class SpringConfiguration {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    @AlfrescoService(ServiceType.LOW_LEVEL)
    private PermissionService permissionService;

    @Autowired
    @AlfrescoService(ServiceType.LOW_LEVEL)
    private org.alfresco.service.cmr.repository.NodeService nodeService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repository;

    @Autowired
    private SolrClient solrClient;

    @Autowired
    @Qualifier("messageService")
    private MessageService messageService;


    @Bean
    public ApixToAlfrescoConversion apixToAlfrescoConversion() {
        return new ApixToAlfrescoConversion(serviceRegistry);
    }

    //Dont register bean, bean already defined at package eu.xenit.apix.alfresco.categories.CategoryService @Component;
    public ICategoryService categoryServiceApix() {
        return new CategoryService(serviceRegistry, apixToAlfrescoConversion());
    }

//    @Bean
//    public INodeService metadataServiceApix() {
//        return new NodeService(serviceRegistry, apixToAlfrescoConversion());
//    }

    @Bean
    public FacetConfiguration facetConfiguration() {
        return new FacetConfiguration(serviceRegistry, repository);
    }

    @Bean(name = "eu.xenit.apix.search.SearchFacetsService42")
    public SearchFacetsService searchFacetsServiceApix() {
//        return new SearchService(serviceRegistry, permissionService, nodeService, tenantService, repository, solrClient, apixToAlfrescoConversion());
        return new SearchFacetsServiceImpl42(serviceRegistry, facetConfiguration());
    }

    @Bean(name = "eu.xenit.apix.search.SearchResultCountService42")
    public SearchResultCountService searchResultCountService() {
        return new SearchResultCountServiceImpl42();
    }
}
