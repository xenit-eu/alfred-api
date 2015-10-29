package eu.xenit.apix.alfresco.search;

import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

import java.util.List;

/**
 * Created by mhgam on 29/06/2016.
 */
public interface SearchFacetsService {

    /**
     * Adds the default facets to the search query parameters
     *
     * @deprecated As of v1.18.1, because Alfresco applies some logic on the ftsQuery string and fails. Use the {@link
     * #addFacetSearchParameters(SearchQuery.FacetOptions facets, SearchParameters searchParameters, String query,
     * SearchSyntaxNode searchNode)} overload instead. This as an additional {@link
     * eu.xenit.apix.search.nodes.SearchSyntaxNode} parameter that allows us to apply the facet-logic on a typed
     * query-object instead of an FTS-string
     */
    @Deprecated
    void addFacetSearchParameters(SearchQuery.FacetOptions opts, SearchParameters sp, String ftsQuery);

    void addFacetSearchParameters(SearchQuery.FacetOptions facets, SearchParameters searchParameters, String query,
            SearchSyntaxNode searchNode);

    List<FacetSearchResult> getFacetResults(SearchQuery.FacetOptions opts, ResultSet rs, SearchParameters sp);
    
}
