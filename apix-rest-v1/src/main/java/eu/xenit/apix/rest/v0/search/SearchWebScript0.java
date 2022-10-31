package eu.xenit.apix.rest.v0.search;

import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.SearchQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchWebScript0 {
    private static final Logger logger = LoggerFactory.getLogger(SearchWebScript0.class);
    private final ISearchService service;

    public SearchWebScript0(ISearchService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/eu/xenit/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SearchQueryResult> execute(@RequestBody final SearchQueryV0 query){
        logger.debug("query: {}", query);
        SearchQueryResult result;
        result = service.query(query.toV1());
        for (eu.xenit.apix.search.FacetSearchResult f : result.getFacets()) {
            f.setName("@" + f.getName());
        }
        return ResponseEntity.ok(result);
    }
}
