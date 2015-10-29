package eu.xenit.apix.search;

/**
 * Service used for executing search queries.
 */
public interface ISearchService {

    /**
     * Execute a given query.
     *
     * @param postQuery The query that is executed.
     * @return The results of the search query.
     */
    SearchQueryResult query(SearchQuery postQuery);

}