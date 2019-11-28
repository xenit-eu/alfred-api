package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchResultCountService;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.OrderBy;
import eu.xenit.apix.search.SearchQuery.OrderBy.Order;
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
    public void TestSearchQueryConsistencyIsTransactionalIfPossible() {

    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrderBy_withMultivalueProperty_throwsIllegalArgumentException() {
        SearchService apixSearchServiceMocked = buildApixSearchServiceWithMocks(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        PropertyService propertyServiceMock_withMultivalueTrue = mock(PropertyService.class);
        PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
        when(propertyDefinition.isMultiValued()).thenReturn(true);
        when(propertyServiceMock_withMultivalueTrue.GetPropertyDefinition(any(QName.class)))
                .thenReturn(propertyDefinition);
        apixSearchServiceMocked.setPropertyService(propertyServiceMock_withMultivalueTrue);

        QueryBuilder builder = new QueryBuilder();
        SearchSyntaxNode node = builder
                .property(ContentModel.PROP_NAME.toPrefixString(), "Company Home")
                .create();
        SearchQuery query = new SearchQuery();
        query.setQuery(node);
        eu.xenit.apix.data.StoreRef apixStore = new eu.xenit.apix.data.StoreRef(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
        query.setWorkspace(apixStore);
        QName apixQname = new QName("cm:multivalued");
        OrderBy multivalueOrdering = new OrderBy(Order.ASCENDING, apixQname);
        ArrayList<OrderBy> orderings = new ArrayList<>();
        orderings.add(multivalueOrdering);
        query.setOrderBy(orderings);
        apixSearchServiceMocked.query(query);
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
        Assert.assertTrue(stores.contains(expectedStore));
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
        eu.xenit.apix.data.StoreRef apixStore = null;
        String storeString = "";
        if (store != null) {
            storeString = store.toString();
            apixStore = new eu.xenit.apix.data.StoreRef(storeString);
        }
        query.setWorkspace(apixStore);

        // Execute the action
        SearchService apixSearchService = buildApixSearchServiceWithMocks(store);
        apixSearchService.query(query);

        return apixSearchService.getSearchService();
    }

    private SearchService buildApixSearchServiceWithMocks(StoreRef store) {
        // Setup of the mock for Alfresco Search Service to verify the query parameters
        org.alfresco.service.cmr.search.SearchService alfrescoSearchService = buildAlfrescoSearchServiceMock();
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.getSearchService())
                .thenReturn(alfrescoSearchService);

        ApixToAlfrescoConversion c = mock(ApixToAlfrescoConversion.class);
        String storeString = "";
        if (store != null) {
            storeString = store.toString();
            when(c.alfresco(new eu.xenit.apix.data.StoreRef(storeString)))
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

        return new SearchService(serviceRegistry.getSearchService(), facetService, c, propertyService);
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
