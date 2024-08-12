package eu.xenit.alfred.api.rest.v1.search;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.SearchQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class SearchWebScript1 extends ApixV1Webscript {

    private final ISearchService service;

    public SearchWebScript1(ISearchService service) {
        this.service = service;
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v1/search")
    public ResponseEntity<?> execute(@RequestBody final SearchQuery query) {
        try {
            return writeJsonResponse(service.query(query));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(400).body(illegalArgumentException.getMessage());
        }
    }
}
