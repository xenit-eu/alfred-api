package eu.xenit.apix.rest.v0.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.v0.RestV0Config;
import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import java.io.IOException;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(families = {RestV0Config.Family}, defaultFormat = "json", value = "Search")
@Component("eu.xenit.apix.rest.v0.search.SearchWebScriptV0")
@Qualifier("eu.xenit.apix.rest.v0.search.SearchWebScriptV0")
@Authentication(AuthenticationType.USER)
public class SearchWebScript0 {

    Logger logger = LoggerFactory.getLogger(SearchWebScript0.class);
    @Autowired
    private ISearchService service;
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Uri(value = "/eu/xenit/search", method = HttpMethod.POST)
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        logger.debug(webScriptRequest.toString());

        ObjectMapper m = new SearchNodeJsonParser().getObjectMapper();
        SearchQueryV0 q;
        try {
            q = m.readValue(webScriptRequest.getContent().getInputStream(), SearchQueryV0.class);
        } catch (Exception e) {
            webScriptResponse.setStatus(500);
            webScriptResponse.getWriter().write("Error occured during reading of search query");
            webScriptResponse.getWriter().write(e.getMessage());
            webScriptResponse.getWriter().write(e.getStackTrace().toString());
            return;
        }
        SearchQueryResult result;
        try {
            result = service.query(q.toV1());
        } catch (Exception e) {
            webScriptResponse.setStatus(500);
            webScriptResponse.getWriter().write("Error occured while searching");
            webScriptResponse.getWriter().write(e.getMessage());
            webScriptResponse.getWriter().write(e.getStackTrace().toString());
            return;
        }

        for (eu.xenit.apix.search.FacetSearchResult f : result.getFacets()) {
            f.setName("@" + f.getName());
        }

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.ObjectWriter wr = mapper.writerWithType(SearchQueryResult.class);
        String retStr = wr.writeValueAsString(result);

        webScriptResponse.setContentType("json");
        webScriptResponse.getWriter().write(retStr);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}
