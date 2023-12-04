//\ifdef{EXAMPLE_IMPORTS}Imports

import eu.xenit.apix.data.QName;
import eu.xenit.apix.data.StoreRef;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQueryConsistency;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import java.util.Arrays;

// \endif \if{0}
class searchQuery {

    public static void main(String[] argv) throws IOException {
//\endif\ifdef{EXAMPLE_SEARCH_QUERY_OPTS}Setting search query options
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.getPaging().setSkip(10); // Skip the first 10 results
        searchQuery.getPaging().setLimit(
                25); // Return maximum 25 results in response to the query. It defaults to 25 results, set to -1 for unlimited results

        searchQuery.getFacets().setEnabled(true); // Enables faceting
        searchQuery.getFacets().setMincount(10); // Show only facet values with a minimum of 10 items matching the facet
        searchQuery.getFacets().setLimit(10); // Show only the top 10 facet values for each facet
        // searchQuery.setWorkspace("workspace-protocol://workspace-identifier"); // Optional parameter, defaults to "workspace://SpacesStore"
        // e.g. searchQuery.setWorkspace(new StoreRef("archive://SpacesStore")); // To search in deleted nodes

        searchQuery.setOrderBy(Arrays.asList(
                new SearchQuery.OrderBy(
                        SearchQuery.OrderBy.Order.ASCENDING,
                        new QName("{http://www.alfresco.org/model/content/1.0}modifier")
                ), // Order by cm:modifier ascending
                new SearchQuery.OrderBy(
                        SearchQuery.OrderBy.Order.DESCENDING,
                        new QName("{http://www.alfresco.org/model/content/1.0}creator")
                ) // Then order by cm:creator descending
        ));

// Set search query consistency to transactional (defaults to eventual consistency)
        searchQuery.setConsistency(SearchQueryConsistency.TRANSACTIONAL);
// searchQuery.setConsistency(SearchQueryConsistency.EVENTUAL);
// \endif\ifdef{EXAMPLE_SEARCH_QUERY_QUERY}Building the search query
        QueryBuilder queryBuilder = new QueryBuilder();
        SearchSyntaxNode query = queryBuilder
                .startAnd()
                .property("{http://www.alfresco.org/model/content/1.0}creator", "admin",
                        true) // Search for cm:creator = admin with an exact match
                .startOr()
                .property("{http://www.alfresco.org/model/content/1.0}created", "2015-01-01T00:00:00+00:00",
                        "2020-08-16T00:00:00+00:00") // Date range query
                .property("{http://www.alfresco.org/model/content/1.0}modified", "2015-01-01T00:00:00+00:00",
                        "2020-08-16T00:00:00+00:00")
                .end()
                .not().term("aspect",
                        "{http://www.alfresco.org/model/system/1.0}hidden") // Possible terms: "type", "aspect", "noderef", "path", "text", "parent", "category", "all"
                .term("type", "{http://www.alfresco.org/model/content/1.0}document")
                .term("path", "/app:company_home/app:shared/*") // All documents in the folder
                .term("path", "/app:company_home/app:shared//*") // All documents in the folder and subfolders
                .term("path", "/app:company_home/app:shared/") // Folder itself
                .term("parent",
                        "workspace://SpacesStore/c4ebd508-b9e3-4c48-9e93-cdd774af8bbc") // All direct children of this noderef
                .term("text", "xenit solutions") // Full text search
                .term("all", "banana") // Search in full text, cm:name, cm:author, cm:creator, cm:modifier
                .end()
                .create();
        searchQuery.setQuery(query);
// FTS Query: cm:creator:"admin" AND (cm:created:"2015-01-01T00:00:00+00:00".."2020-08-16T00:00:00+00:00" OR cm:modified:"2015-01-01T00:00:00+00:00".."2020-08-16T00:00:00+00:00") AND NOT ASPECT:"sys:hidden"
// \endif \if{0}
        System.out.println(SearchSyntaxPrinter.Print(query));
    }
}
// \endif
