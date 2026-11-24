package eu.xenit.alfred.api.rest.v0.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQueryResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class SearchWebScriptTest {

    private final ISearchService searchService = mock(ISearchService.class);

    @Test
    void runsTheQueryAsV1AndPrefixesFacetNames() throws IOException {
        FacetSearchResult facet = new FacetSearchResult();
        facet.setName("cm:creator");
        SearchQueryResult result = new SearchQueryResult();
        result.setFacets(List.of(facet));
        when(searchService.query(any())).thenReturn(result);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);

        new SearchWebScript(searchService).execute(requestWithBody(
                "{\"query\":{\"type\":\"cm:content\"},\"limit\":15,\"skip\":5,"
                        + "\"orderBy\":[{\"property\":\"cm:name\",\"order\":\"descending\"}]}"), res);

        ArgumentCaptor<SearchQuery> query = ArgumentCaptor.forClass(SearchQuery.class);
        verify(searchService).query(query.capture());
        assertEquals(15, query.getValue().getPaging().getLimit());
        assertEquals(5, query.getValue().getPaging().getSkip());
        assertEquals(SearchQuery.OrderBy.Order.DESCENDING, query.getValue().getOrderBy().get(0).getOrder());
        assertEquals(true, query.getValue().getFacets().isEnabled());

        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
        JsonNode body = new ObjectMapper().readTree(out.toString(StandardCharsets.UTF_8));
        assertEquals("@cm:creator", body.get("facets").get(0).get("name").asText());
    }

    @Test
    void missingOrInvalidBodyIs400() {
        SearchWebScript webScript = new SearchWebScript(searchService);
        for (String body : new String[]{null, " ", "not json"}) {
            WebScriptException e = assertThrows(WebScriptException.class,
                    () -> webScript.execute(requestWithBody(body), mock(WebScriptResponse.class)), String.valueOf(body));
            assertEquals(400, e.getStatus(), String.valueOf(body));
        }
        verifyNoInteractions(searchService);
    }

    /** A request whose body is {@code body}; {@code null} means the request has no body. */
    private static WebScriptRequest requestWithBody(String body) throws IOException {
        WebScriptRequest req = mock(WebScriptRequest.class);
        if (body != null) {
            Content content = mock(Content.class);
            when(content.getContent()).thenReturn(body);
            when(req.getContent()).thenReturn(content);
        }
        return req;
    }
}