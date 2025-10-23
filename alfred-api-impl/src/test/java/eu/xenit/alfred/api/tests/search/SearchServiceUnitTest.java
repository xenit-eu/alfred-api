package eu.xenit.alfred.api.tests.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.alfresco.dictionary.PropertyService;
import eu.xenit.alfred.api.alfresco.search.SearchFacetsService;
import eu.xenit.alfred.api.alfresco.search.SearchResultCountService;
import eu.xenit.alfred.api.alfresco.search.SearchService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQuery.FacetOptions;
import eu.xenit.alfred.api.search.SearchQuery.OrderBy;
import eu.xenit.alfred.api.search.SearchQuery.OrderBy.Order;
import eu.xenit.alfred.api.search.SearchQuery.PagingOptions;
import eu.xenit.alfred.api.search.SearchQueryConsistency;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import java.util.ArrayList;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class SearchServiceUnitTest {

    @Test
    public void TestSearchInDefaultStore() {
        assertAlfrescoSearchQueryStoreMatchesInput(null, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    @Test
    public void TestSearchInWorkspaceSpacesStoreStore() {
        assertAlfrescoSearchQueryStoreMatchesInput(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    @Test
    public void TestSearchInArchiveSpacesStoreStore() {
        assertAlfrescoSearchQueryStoreMatchesInput(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
                StoreRef.STORE_REF_ARCHIVE_SPACESSTORE);
    }

    @Test
    public void testDefaultSearchQueryConsistencyIsTransactionalIfPossible() {
        SearchQuery searchQuery = new SearchQuery();
        Assertions.assertEquals(SearchQueryConsistency.TRANSACTIONAL_IF_POSSIBLE, searchQuery.getConsistency());
    }

    @Test
    public void testSearchQueryConsistencyIsSetToEventualIfFacetsIsEnabled() {
        org.alfresco.service.cmr.search.SearchService alfrescoSearchServiceMock = buildAlfrescoSearchServiceMock();
        SearchService alfredApiSearchServiceMocked = buildAlfredApiSearchServiceWithMocks(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, alfrescoSearchServiceMock);

        SearchQuery searchQuery = new SearchQuery();

        //Setting query consistency to TRANSACTIONAL_IF_POSSIBLE
        searchQuery.setConsistency(SearchQueryConsistency.TRANSACTIONAL_IF_POSSIBLE);

        //Enabling facets in the facet options
        FacetOptions facetOptions = new FacetOptions();
        facetOptions.setEnabled(true);
        searchQuery.setFacets(facetOptions);

        //Setting up a basic search query
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(ContentModel.PROP_NAME.toPrefixString(), "Company Home")
                .create();
        searchQuery.setQuery(node);

        //Setting workspace
        eu.xenit.alfred.api.data.StoreRef alfredApiStore = new eu.xenit.alfred.api.data.StoreRef(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
        searchQuery.setWorkspace(alfredApiStore);

        //Executing query and checking query consistency given to Alfresco search service
        ArgumentCaptor<SearchParameters> searchParamsArgument = ArgumentCaptor.forClass(SearchParameters.class);
        alfredApiSearchServiceMocked.query(searchQuery);
        verify(alfrescoSearchServiceMock).query(searchParamsArgument.capture());
        Assertions.assertEquals(QueryConsistency.EVENTUAL, searchParamsArgument.getValue().getQueryConsistency());
    }

    @Test
    public void testOrderBy_withMultivalueProperty_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
()->{
            SearchService alfredApiSearchServiceMocked = buildAlfredApiSearchServiceWithMocks(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

            PropertyService propertyServiceMock_withMultivalueTrue = mock(PropertyService.class);
            PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
            when(propertyDefinition.isMultiValued()).thenReturn(true);
            when(propertyServiceMock_withMultivalueTrue.GetPropertyDefinition(any(QName.class)))
                    .thenReturn(propertyDefinition);
            alfredApiSearchServiceMocked.setPropertyService(propertyServiceMock_withMultivalueTrue);

            QueryBuilder builder = new QueryBuilder();
            SearchSyntaxNode node = builder
                    .property(ContentModel.PROP_NAME.toPrefixString(), "Company Home")
                    .create();
            SearchQuery query = new SearchQuery();
            query.setQuery(node);
            eu.xenit.alfred.api.data.StoreRef alfredApiStore = new eu.xenit.alfred.api.data.StoreRef(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
            query.setWorkspace(alfredApiStore);
            QName alfredApiQname = new QName("cm:multivalued");
            OrderBy multivalueOrdering = new OrderBy(Order.ASCENDING, alfredApiQname);
            ArrayList<OrderBy> orderings = new ArrayList<>();
            orderings.add(multivalueOrdering);
            query.setOrderBy(orderings);
            alfredApiSearchServiceMocked.query(query);
        }, "Expected an IllegalArgumentException to be thrown by testOrderBy_withMultivalueProperty_throwsIllegalArgumentException");
    }

    @Test
    public void testLimitDefault() {
        SearchService searchService = buildAlfredApiSearchServiceWithMocks(null);

        SearchQuery query = new SearchQuery();
        query.setQuery(new QueryBuilder().property("foo", "bar").create());

        Assertions.assertEquals(1000, searchService.buildSearchParameters(query).getMaxItems());
    }

    @Test
    public void testLimit() {
        SearchService searchService = buildAlfredApiSearchServiceWithMocks(null);

        SearchQuery query = new SearchQuery();
        query.setQuery(new QueryBuilder().property("foo", "bar").create());
        query.setPaging(new PagingOptions(26000, 0));

        Assertions.assertEquals(26000, searchService.buildSearchParameters(query).getMaxItems());
    }

    private void assertAlfrescoSearchQueryStoreMatchesInput(StoreRef store, StoreRef expectedStore) {
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = searchInStore(store);

        // Validate that the Alfresco Search Service has received a query with the correct StoreRef
        ArgumentCaptor<SearchParameters> argument = ArgumentCaptor
                .forClass(SearchParameters.class);
        verify(alfrescoSearchService)
                .query(argument.capture());

        ArrayList<StoreRef> stores = argument
                .getValue()
                .getStores();
        Assertions.assertTrue(stores.contains(expectedStore));
    }

    private org.alfresco.service.cmr.search.SearchService searchInStore(StoreRef store) {
        //<editor-fold desc="Create a simple query">
        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(ContentModel.PROP_NAME.toPrefixString(), "Company Home")
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        //</editor-fold>

        // Set the test StoreRef
        eu.xenit.alfred.api.data.StoreRef alfredApiStore = null;
        String storeString = "";
        if (store != null) {
            storeString = store.toString();
            alfredApiStore = new eu.xenit.alfred.api.data.StoreRef(storeString);
        }
        query.setWorkspace(alfredApiStore);

        // Execute the action
        SearchService alfredApiSearchService = buildAlfredApiSearchServiceWithMocks(store);
        alfredApiSearchService.query(query);

        return alfredApiSearchService.getSearchService();
    }

    private SearchService buildAlfredApiSearchServiceWithMocks(StoreRef store,
            org.alfresco.service.cmr.search.SearchService alfrescoSearchServiceMock) {
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.getSearchService())
                .thenReturn(alfrescoSearchServiceMock);

        AlfredApiToAlfrescoConversion c = mock(AlfredApiToAlfrescoConversion.class);
        String storeString = "";
        if (store != null) {
            storeString = store.toString();
            when(c.alfresco(new eu.xenit.alfred.api.data.StoreRef(storeString)))
                    .thenReturn(new StoreRef(storeString));
        }

        SearchFacetsService facetService = mock(SearchFacetsService.class);
        when(facetService.getFacetResults(any(SearchQuery.FacetOptions.class), any(ResultSet.class),
                any(SearchParameters.class)))
                .thenReturn(new ArrayList<FacetSearchResult>());

        SearchResultCountService resultCountService = mock(SearchResultCountService.class);
        when(resultCountService
                .countResults(any(SearchQuery.PagingOptions.class), any(ResultSet.class), any(SearchParameters.class)))
                .thenReturn(1L);

        PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
        when(propertyDefinition.isMultiValued()).thenReturn(false);
        PropertyService propertyService = mock(PropertyService.class);
        when(propertyService.GetPropertyDefinition(any(QName.class))).thenReturn(propertyDefinition);

        return new SearchService(serviceRegistry.getSearchService(), new Properties(), facetService, c, propertyService);
    }

    private SearchService buildAlfredApiSearchServiceWithMocks(StoreRef store) {
        // Setup of the mock for Alfresco Search Service to verify the query parameters
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = buildAlfrescoSearchServiceMock();
        return buildAlfredApiSearchServiceWithMocks(store, alfrescoSearchService);
    }

    private org.alfresco.service.cmr.search.SearchService buildAlfrescoSearchServiceMock() {
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = mock(
                org.alfresco.service.cmr.search.SearchService.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.iterator())
                .thenReturn(new ArrayList<ResultSetRow>().iterator());
        when(alfrescoSearchService.query(any(SearchParameters.class)))
                .thenReturn(rs);
        return alfrescoSearchService;
    }
}
