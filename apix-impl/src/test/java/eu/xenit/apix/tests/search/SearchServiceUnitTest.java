package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import java.util.ArrayList;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchServiceUnitTest {
    @Test
    public void TestSearchInDefaultStore(){
        assertAlfrescoSearchQueryStoreMatchesInput(null, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    @Test
    public void TestSearchInWorkspaceSpacesStoreStore(){
        assertAlfrescoSearchQueryStoreMatchesInput(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    @Test
    public void TestSearchInArchiveSpacesStoreStore(){
        assertAlfrescoSearchQueryStoreMatchesInput(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, StoreRef.STORE_REF_ARCHIVE_SPACESSTORE);
    }

    private void assertAlfrescoSearchQueryStoreMatchesInput(StoreRef store, StoreRef expectedStore){
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = searchInStore(store);

        // Validate that the Alfresco Search Service has received a query with the correct StoreRef
        ArgumentCaptor<SearchParameters> argument = ArgumentCaptor
                                                    .forClass(SearchParameters.class);
        verify(alfrescoSearchService)
            .query(argument.capture());

        ArrayList<StoreRef> stores = argument
                                        .getValue()
                                        .getStores();
        Assert.assertTrue(stores.contains(expectedStore));
    }

    private org.alfresco.service.cmr.search.SearchService searchInStore(StoreRef store){
        //<editor-fold desc="Create a simple query">
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(ContentModel.PROP_NAME.toPrefixString(), "Company Home")
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        //</editor-fold>

        // Set the test StoreRef
        eu.xenit.apix.data.StoreRef apixStore = null;
        String storeString = "";
        if(store != null){
            storeString = store.toString();
            apixStore = new eu.xenit.apix.data.StoreRef(storeString);
        }
        query.setWorkspace(apixStore);

        // Setup of the mock for Alfresco Search Service to verify the query parameters
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = mock(org.alfresco.service.cmr.search.SearchService.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.getSearchService())
            .thenReturn(alfrescoSearchService);

        //<editor-fold desc="Necessary dependencies to complete the Apix searchService.query">
        ResultSet rs = mock(ResultSet.class);
        when(rs.iterator())
            .thenReturn(new ArrayList<ResultSetRow>().iterator());
        when(alfrescoSearchService.query(any(SearchParameters.class)))
            .thenReturn(rs);

        ApixToAlfrescoConversion c = mock(ApixToAlfrescoConversion.class);
        if(store != null){
            when(c.alfresco(new eu.xenit.apix.data.StoreRef(storeString)))
                .thenReturn(new StoreRef(storeString));
        }

        SearchFacetsService facetService = mock(SearchFacetsService.class);
        when(facetService.getFacetResults(any(SearchQuery.FacetOptions.class), any(ResultSet.class), any(SearchParameters.class)))
            .thenReturn(new ArrayList<FacetSearchResult>());

        SearchResultCountService resultCountService = mock(SearchResultCountService.class);
        when(resultCountService.countResults(any(SearchQuery.PagingOptions.class), any(ResultSet.class), any(SearchParameters.class)))
            .thenReturn(1L);
        //</editor-fold>

        // Execute the action
        new SearchService(serviceRegistry.getSearchService(), facetService, c).query(query);

        return alfrescoSearchService;
    }
}
