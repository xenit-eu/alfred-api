package eu.xenit.alfred.api.rest.v1.bulk.request;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class BulkHttpServletRequestTest {

    private final MockHttpServletRequest bulkRequest = new MockHttpServletRequest("POST",
            "/alfresco/service/apix/v1/bulk");

    @Test
    void methodPathAndParametersComeFromTheSubRequest() throws IOException {
        bulkRequest.addParameter("userName", "fromTheBulkRequest");
        bulkRequest.addHeader("Authorization", "Basic abc");

        BulkHttpServletRequest request = new BulkHttpServletRequest(bulkRequest,
                "/people?userName=a%26b&tag=1&tag=2&name=first+last", "GET", null);

        assertEquals("GET", request.getMethod());
        assertEquals("/alfresco/service/apix/v1/people", request.getRequestURI());
        assertEquals("userName=a%26b&tag=1&tag=2&name=first+last", request.getQueryString());
        assertEquals("a&b", request.getParameter("userName"));
        assertArrayEquals(new String[]{"1", "2"}, request.getParameterValues("tag"));
        assertEquals("first last", request.getParameter("name"));
        assertEquals(3, request.getParameterMap().size());
        // Headers are still the bulk request's, as before.
        assertEquals("Basic abc", request.getHeader("Authorization"));
    }

    @Test
    void withoutAQueryStringThereAreNoParameters() throws IOException {
        bulkRequest.addParameter("leaked", "no");

        BulkHttpServletRequest request = new BulkHttpServletRequest(bulkRequest, "/version", "GET", null);

        assertNull(request.getQueryString());
        assertNull(request.getParameter("leaked"));
        assertFalse(request.getParameterNames().hasMoreElements());
    }

    @Test
    void theServletPathsDescribeTheSubRequest() throws IOException {
        String path = "/properties/%7Bhttp%3A%2F%2Fwww.alfresco.org%2Fmodel%2Fcontent%2F1.0%7Dname";

        BulkHttpServletRequest request = new BulkHttpServletRequest(bulkRequest, path + "?a=b", "GET", null);

        // The URI stays encoded, as in a real request; the web script runtime decodes it once.
        assertEquals("/alfresco/service/apix/v1" + path, request.getRequestURI());
        assertEquals("http://localhost/alfresco/service/apix/v1" + path, request.getRequestURL().toString());
        assertEquals("/alfresco", request.getContextPath());
        assertEquals("/service", request.getServletPath());
        assertEquals("/apix/v1/properties/{http://www.alfresco.org/model/content/1.0}name", request.getPathInfo());
    }

    @Test
    void queryStringParsingFollowsTheServletContainer() {
        Map<String, String[]> parameters = BulkHttpServletRequest.parseQueryString("flag&bad=%zz&=x&&ok=%C3%A9");

        assertArrayEquals(new String[]{""}, parameters.get("flag"));
        assertArrayEquals(new String[]{"é"}, parameters.get("ok"));
        assertEquals(2, parameters.size(), "a malformed or nameless pair is skipped");
    }

    @Test
    void theBodyIsSentAsJsonWithItsLength() throws IOException {
        BulkHttpServletRequest request = new BulkHttpServletRequest(bulkRequest, "/nodes/nodeInfo", "POST",
                new ObjectMapper().readTree("{\"name\":\"Україна\"}"));

        byte[] expected = "{\"name\":\"Україна\"}".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, request.getInputStream().readAllBytes());
        assertEquals(expected.length, request.getContentLength());
        // Each read gets the whole body, as the runtime may read it more than once on a transaction retry.
        assertArrayEquals(expected, request.getInputStream().readAllBytes());
        assertEquals("{\"name\":\"Україна\"}", request.getReader().readLine());
    }

    @Test
    void aMissingBodyIsSentAsJsonNull() throws IOException {
        BulkHttpServletRequest request = new BulkHttpServletRequest(bulkRequest, "/version", "GET", null);

        assertEquals("null", new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }
}
