package eu.xenit.apix.webscripts;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;

public class AnnotationWebScriptRequest implements WrappingWebScriptRequest {

    // (val webScriptRequest: WebScriptRequest) : WebScriptRequest by webScriptRequest, WrappingWebScriptRequest {
    private WebScriptRequest webScriptRequest;

    private Map<String, Object> model = new LinkedHashMap<>();
    private Throwable thrownException = null;

    public AnnotationWebScriptRequest(WebScriptRequest webScriptRequest) {
        this.webScriptRequest = webScriptRequest;
    }


    public WebScriptRequest getWebScriptRequest() {
        return webScriptRequest;
    }

    public void setWebScriptRequest(WebScriptRequest webScriptRequest) {
        this.webScriptRequest = webScriptRequest;
    }

    public Throwable getThrownException() {
        return thrownException;
    }

    public void setThrownException(Throwable thrownException) {
        this.thrownException = thrownException;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return getNext().toString();
    }

    @Override
    public WebScriptRequest getNext() {
        if (webScriptRequest instanceof WrappingWebScriptRequest) {
            return ((WrappingWebScriptRequest) webScriptRequest).getNext();
        } else {
            return webScriptRequest;
        }
    }

    @Override
    public Match getServiceMatch() {
        return webScriptRequest.getServiceMatch();
    }

    @Override
    public String getServerPath() {
        return webScriptRequest.getServerPath();
    }

    @Override
    public String getContextPath() {
        return webScriptRequest.getContextPath();
    }

    @Override
    public String getServiceContextPath() {
        return webScriptRequest.getServiceContextPath();
    }

    @Override
    public String getServicePath() {
        return webScriptRequest.getServicePath();
    }

    @Override
    public String getURL() {
        return webScriptRequest.getURL();
    }

    @Override
    public String getPathInfo() {
        return webScriptRequest.getPathInfo();
    }

    @Override
    public String getQueryString() {
        return webScriptRequest.getQueryString();
    }

    @Override
    public String[] getParameterNames() {
        return webScriptRequest.getParameterNames();
    }

    @Override
    public String getParameter(String name) {
        return webScriptRequest.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return webScriptRequest.getParameterValues(name);
    }

    @Override
    public String[] getHeaderNames() {
        return webScriptRequest.getHeaderNames();
    }

    @Override
    public String getHeader(String name) {
        return webScriptRequest.getHeader(name);
    }

    @Override
    public String[] getHeaderValues(String name) {
        return webScriptRequest.getHeaderValues(name);
    }

    @Override
    public String getExtensionPath() {
        return webScriptRequest.getExtensionPath();
    }

    @Override
    public String getContentType() {
        return webScriptRequest.getContentType();
    }

    @Override
    public Content getContent() {
        return webScriptRequest.getContent();
    }

    @Override
    public Object parseContent() {
        return webScriptRequest.parseContent();
    }

    @Override
    public boolean isGuest() {
        return webScriptRequest.isGuest();
    }

    @Override
    public String getFormat() {
        return webScriptRequest.getFormat();
    }

    @Override
    public Description.FormatStyle getFormatStyle() {
        return webScriptRequest.getFormatStyle();
    }

    @Override
    public String getAgent() {
        return webScriptRequest.getAgent();
    }

    @Override
    public String getJSONCallback() {
        return webScriptRequest.getJSONCallback();
    }

    @Override
    public boolean forceSuccessStatus() {
        return webScriptRequest.forceSuccessStatus();
    }

    @Override
    public Runtime getRuntime() {
        return webScriptRequest.getRuntime();
    }



   /* public val model: MutableMap<String, Any> = LinkedHashMap()

    public var thrownException: Throwable? = null
        set

    override fun getNext(): WebScriptRequest {
        if (webScriptRequest is WrappingWebScriptRequest) {
            return webScriptRequest.next
        } else {
            return webScriptRequest
        }
    }*/
}
