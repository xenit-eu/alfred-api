package eu.xenit.apix.rest.v1.bulk.request;

import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntermediateRequest extends WebScriptServletRequest {


    public IntermediateRequest(WebScriptRequest bulkWebScriptRequest, HttpServletRequest request, Match match) {
        super(bulkWebScriptRequest.getRuntime(), request, match, null);
    }


    @Override
    public String getExtensionPath() {
        String uri = getHttpServletRequest().getRequestURI();
        List<String> uriSplit = new ArrayList<>(Arrays.asList(uri.split("/")));
        uriSplit.remove(0);
        uriSplit.remove(0);
        uriSplit.remove(0);
        uriSplit.remove(0);
        return String.join("/", uriSplit);
    }


}
