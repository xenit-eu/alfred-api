package eu.xenit.apix.rest.v1.bulk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptRequestURLImpl;

public class IntermediateRequest extends WebScriptRequestURLImpl {

    private String serverPath;
    private Map<String, List<String>> headers;
    private String agent;
    private Content content;

    public IntermediateRequest(WebScriptRequest bulkWebScriptRequest, String URL, Content content, Match match) {
        super(bulkWebScriptRequest.getRuntime(), splitURL(bulkWebScriptRequest.getContextPath(), URL), match);
        serverPath = bulkWebScriptRequest.getServerPath();
        this.content = content;
        agent = bulkWebScriptRequest.getAgent();
        headers = new HashMap<>();
        for (String headerName : bulkWebScriptRequest.getHeaderNames()) {
            String[] values = bulkWebScriptRequest.getHeaderValues(headerName);
            List<String> list = Arrays.asList(values);
            headers.put(headerName, list);
        }
    }

    @Override
    public String getServerPath() {
        return serverPath;
    }

    @Override
    public String[] getHeaderNames() {
        Set<String> names = headers.keySet();
        return names.toArray(new String[names.size()]);
    }

    @Override
    public String getHeader(String name) {
        List<String> list = headers.get(name);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public String[] getHeaderValues(String name) {
        List<String> list = headers.get(name);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public String getAgent() {
        return agent;
    }

}
