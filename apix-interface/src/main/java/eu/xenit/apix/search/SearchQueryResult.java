package eu.xenit.apix.search;


import eu.xenit.apix.data.NodeRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Datastructure that represents the result of a search.
 * noderefs: The list of noderefs that fulfill the search.
 * facets: The facets available on the search result.
 */
public class SearchQueryResult {

    private List<String> noderefs = new ArrayList<>();
    private List<FacetSearchResult> facets;
    public long totalResultCount;
    private Highlights highlights;

    public List<String> getNoderefs() {
        return noderefs;
    }

    public void setNoderefs(List<String> noderefs) {
        this.noderefs = noderefs;
    }

    public void addResult(NodeRef nodeRef) {
        noderefs.add(nodeRef.toString());
    }

    public List<FacetSearchResult> getFacets() {
        return facets;
    }

    public void setFacets(List<FacetSearchResult> facets) {
        this.facets = facets;
    }

    /**
     * Returns the total number of results for the search query. Depending on if the query is executed
     * against the database or Solr this will include the skipped results or not.
     */
    public long getTotalResultCount() {
        return totalResultCount;
    }

    public void setTotalResultCount(long totalResultCount) {
        this.totalResultCount = totalResultCount;
    }

    public Highlights getHighlights() {
        return highlights;
    }

    public void setHighlights(Highlights highlights) {
        this.highlights = highlights;
    }

    @Override
    public String toString() {
        return "SearchQueryResult{" +
                "totalResultCount=" + totalResultCount +
                ", noderefs=" + noderefs +
                ", facets=" + facets +
                ", highlights=" + highlights +
                '}';
    }
}
