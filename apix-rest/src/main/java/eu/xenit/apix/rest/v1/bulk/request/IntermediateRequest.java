package eu.xenit.apix.rest.v1.bulk.request;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

public class IntermediateRequest extends WebScriptServletRequest {

    public static final String MATCH = "apix/";

    public IntermediateRequest(WebScriptRequest bulkWebScriptRequest, HttpServletRequest request) {
        super(bulkWebScriptRequest.getRuntime(), request, null, null);
    }


    @Override
    public String getExtensionPath() {
        String uri = getHttpServletRequest().getRequestURI();

        if (uri.contains(MATCH)) {
            uri = uri.substring(uri.indexOf(MATCH) + MATCH.length());
        }

        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }

        return uri;
    }


}
