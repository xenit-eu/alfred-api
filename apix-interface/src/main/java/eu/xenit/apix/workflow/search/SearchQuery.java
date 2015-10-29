package eu.xenit.apix.workflow.search;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchQuery {

    public boolean isActive = true;
    public List<IQueryFilter> filters;
    public List<String> facets;
    public List<Sorting> orderBy;
    public boolean includeRefs = true;
    public boolean includeResults = true;
    public Paging paging;

    public SearchQuery() {
    }

    public SearchQuery(SearchQuery source) {
        this.paging = source.paging;
        if (source.filters != null) {
            this.filters = new ArrayList<>(source.filters);
        }
        if (source.facets != null) {
            this.facets = new ArrayList<>(source.facets);
        }
        if (source.orderBy != null) {
            this.orderBy = new ArrayList<>(source.orderBy);
        }
        this.includeRefs = source.includeRefs;
        this.includeResults = source.includeResults;
        this.isActive = source.isActive;
    }

    public abstract void restrictResultsToUser(String currentUserName);
}
