package eu.xenit.alfred.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

/**
 * Performs requests in REST integration tests and asserts the response contract.
 */
public final class RestCall {

    private RestCall() {
    }

    public static Result get(String url) throws IOException {
        return execute(Request.Get(url));
    }

    public static Result put(String url, ContentType contentType, String body) throws IOException {
        return execute(Request.Put(url).bodyString(body, contentType));
    }

    public static Result post(String url, ContentType contentType, String body) throws IOException {
        return execute(Request.Post(url).bodyString(body, contentType));
    }

    private static Result execute(Request request) throws IOException {
        HttpResponse response = request.execute().returnResponse();
        byte[] body = response.getEntity() == null ? new byte[0] : EntityUtils.toByteArray(response.getEntity());
        return new Result(response.getStatusLine().getStatusCode(), header(response, "Content-Type"),
                header(response, "Cache-Control"), body);
    }

    private static String header(HttpResponse response, String name) {
        Header header = response.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    /** Asserts a 200 JSON response with the expected API headers and returns the parsed body. */
    public static JsonNode assertJson(Result result) throws IOException {
        assertEquals("Unexpected status, response body: " + result.text(), 200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("no-cache", result.cacheControl());
        return new ObjectMapper().readTree(result.body());
    }

    /** Asserts a plain-text response with the given status and message, decoded with its declared charset. */
    public static void assertText(Result result, int status, String message) {
        assertEquals("Unexpected status, response body: " + result.text(), status, result.status());
        assertNotNull("Response has no Content-Type header", result.contentType());
        assertTrue("Expected text/plain but was " + result.contentType(),
                result.contentType().startsWith("text/plain"));
        assertEquals("no-cache", result.cacheControl());
        assertEquals(message, result.text());
    }

    /** Asserts that a plain-text response declares UTF-8 and that its bytes are the UTF-8 encoding of the message. */
    public static void assertUtf8Text(Result result, int status, String message) {
        assertText(result, status, message);
        assertEquals("text/plain;charset=UTF-8", result.contentType());
        assertEquals(message, new String(result.body(), StandardCharsets.UTF_8));
    }

    public record Result(int status, String contentType, String cacheControl, byte[] body) {

        /** The body decoded with the charset declared in Content-Type, or UTF-8 (the JSON encoding) if none is. */
        public String text() {
            Charset charset = contentType == null ? null : ContentType.parse(contentType).getCharset();
            return new String(body, charset == null ? StandardCharsets.UTF_8 : charset);
        }
    }
}
