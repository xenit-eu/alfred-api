package eu.xenit.apix.rest.v1.bulk.request;

import org.springframework.util.StringUtils;

// javax.servlet => jakarta.servlet
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

public class BulkHttpServletRequest extends HttpServletRequestWrapper {
    private static final String HTTP = "http";

    private static final String HTTPS = "https";
    private static final String ALFRESCO = "/alfresco";
    private static final String SERVICE = "/service";

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the {@link HttpServletRequest} to be wrapped.
     * @throws IllegalArgumentException if the request is null
     */
    public BulkHttpServletRequest(HttpServletRequest request, String url, String method, Object body) {
        super(request);
        this.url = url;
        this.method = method;
        this.body = body;
    }

    final String url;
    final String method;
    final Object body;

    @Override
    public String getRequestURI() {
        return url;
    }

    @Override
    public String getContextPath() {
        return ALFRESCO;
    }

    @Override
    public String getServletPath() {
        return SERVICE;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new IntermediateContent(body);
    }

    public Object getBody() {
        return body;
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        String server = getServerName();
        int port = getServerPort();
        String uri = getRequestURI();

        StringBuffer sb = new StringBuffer(scheme).append("://").append(server);
        if (port > 0 && ((HTTP.equalsIgnoreCase(scheme) && port != 80) ||
                (HTTPS.equalsIgnoreCase(scheme) && port != 443))) {
            sb.append(':').append(port);
        }
        if (StringUtils.hasText(uri)) {
            sb.append(uri);
        }
        return sb;
    }
}
