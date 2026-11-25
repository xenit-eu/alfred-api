package eu.xenit.alfred.api.rest.v0.search;

import eu.xenit.alfred.api.rest.webscript.JsonRequests;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.SearchQueryResult;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * The v0 search: takes a {@link SearchQueryV0}, runs it as a v1 search, and prefixes every facet name with "@".
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.search.search.post")
public class SearchWebScript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(SearchWebScript.class);

    private final ISearchService searchService;

    public SearchWebScript(ISearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        SearchQueryV0 query = JsonRequests.readBody(req, SearchQueryV0.class);
        logger.debug("query: {}", query);

        SearchQueryResult result = searchService.query(query.toV1());
        for (FacetSearchResult facet : result.getFacets()) {
            facet.setName("@" + facet.getName());
        }
        JsonResponses.writeJson(res, result);
    }
}