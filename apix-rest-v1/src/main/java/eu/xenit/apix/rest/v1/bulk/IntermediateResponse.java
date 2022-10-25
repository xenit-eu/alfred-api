package eu.xenit.apix.rest.v1.bulk;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptResponseImpl;

public class IntermediateResponse extends WebScriptResponseImpl {

    private Writer out;
    private int status = 200; // Webscripts default to success, explicit set status on failure
    private Map<String, String> headers;

    public IntermediateResponse(Runtime runtime) {
        super(runtime);
        out = new StringWriter();
        headers = new HashMap<>();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public OutputStream getOutputStream() throws IOException {
        // TODO: implement this?
        return null;
    }

    public Writer getWriter() throws IOException {
        return out;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addHeader(String name, String value) {
        setHeader(name, value);
    }

    public String encodeScriptUrl(String url) {
        // not supported
        throw new UnsupportedOperationException();
    }

    public String getEncodeScriptUrlFunction(String name) {
        // not supported
        throw new UnsupportedOperationException();
    }

    public void reset() {
        // not supported
    }

    @Override
    public void reset(String preserveHeadersPattern) {
        // not supported
    }

    public void setCache(Cache cache) {
        // not supported
    }

    public void setContentType(String contentType) {
        // not supported
    }

    public void setContentEncoding(String contentEncoding) {
        // not supported
    }

}
