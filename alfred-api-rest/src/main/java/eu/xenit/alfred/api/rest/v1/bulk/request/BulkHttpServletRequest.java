package eu.xenit.alfred.api.rest.v1.bulk.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.util.StringUtils;

/**
 * A bulk sub-request as an HTTP request for {@code /alfresco/service/apix/v1} + its URL. The method, path, query
 * parameters and body are the sub-request's; everything else, including the headers, is the bulk request's.
 */
public class BulkHttpServletRequest extends HttpServletRequestWrapper {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";
    private static final String ALFRESCO = "/alfresco";
    private static final String SERVICE = "/service";
    private static final String API_V1 = "/apix/v1";

    private static final ObjectMapper BODY_MAPPER = new ObjectMapper();

    private final String requestUri;
    private final String queryString;
    private final Map<String, String[]> parameters;
    private final String method;
    private final byte[] content;

    /**
     * @param url  relative to {@code /apix/v1}, with an optional query string
     * @param body sent as JSON; a missing body is sent as {@code null}, as before
     */
    public BulkHttpServletRequest(HttpServletRequest request, String url, String method, JsonNode body)
            throws JsonProcessingException {
        super(request);
        int queryStart = url == null ? -1 : url.indexOf('?');
        // The path stays encoded, as in a real request: the web script runtime decodes it.
        this.requestUri = ALFRESCO + SERVICE + API_V1 + (queryStart < 0 ? url : url.substring(0, queryStart));
        this.queryString = queryStart < 0 ? null : url.substring(queryStart + 1);
        this.parameters = parseQueryString(queryString);
        this.method = method;
        this.content = BODY_MAPPER.writeValueAsBytes(body);
    }

    /**
     * Parses a query string as Tomcat does: UTF-8, '+' is a space, names may repeat and a malformed pair is skipped.
     */
    static Map<String, String[]> parseQueryString(String query) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                int separator = pair.indexOf('=');
                String name = separator < 0 ? pair : pair.substring(0, separator);
                if (name.isEmpty()) {
                    continue;
                }
                try {
                    String value = separator < 0 ? "" : decode(pair.substring(separator + 1));
                    values.computeIfAbsent(decode(name), key -> new ArrayList<>()).add(value);
                } catch (IllegalArgumentException malformedEscape) {
                }
            }
        }
        Map<String, String[]> parameters = new LinkedHashMap<>();
        values.forEach((name, list) -> parameters.put(name, list.toArray(new String[0])));
        return Collections.unmodifiableMap(parameters);
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public String getContextPath() {
        return ALFRESCO;
    }

    @Override
    public String getServletPath() {
        return SERVICE;
    }

    /** Decoded, like a servlet container's path info; the runtime decodes {@link #getRequestURI()} the same way. */
    @Override
    public String getPathInfo() {
        return URLDecoder.decode(requestUri.substring((ALFRESCO + SERVICE).length()));
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        return values == null ? null : values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = parameters.get(name);
        return values == null ? null : values.clone();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new IntermediateContent(content);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() {
        return content.length;
    }

    @Override
    public long getContentLengthLong() {
        return content.length;
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
