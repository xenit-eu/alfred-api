package eu.xenit.alfred.api.rest.v0.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

/**
 * Request handling shared by the v0 metadata web scripts, reproducing how Spring MVC bound
 * {@code @RequestParam NodeRef nodeRef} and {@code @RequestBody} in the original implementation.
 */
final class MetadataRequests {

    // The same Jackson configuration the MVC message converter used to read request bodies. Fully configured by the
    // factory and never modified afterwards, so it is safe to share between threads.
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getNewObjectMapper();

    private static final MediaType JSON_SUBTYPES = new MediaType("application", "*+json");

    private MetadataRequests() {
    }

    /**
     * The required {@code nodeRef} query parameter. A missing parameter is a 400; any value that is present (even
     * empty or malformed) is passed on, so the service rejects it exactly as before.
     */
    static NodeRef nodeRef(WebScriptRequest req) {
        String value = req.getParameter("nodeRef");
        if (value == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required parameter 'nodeRef' is not present.");
        }
        return new NodeRef(value);
    }

    static <T> T readBody(WebScriptRequest req, Class<T> type) throws IOException {
        return readBody(req, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    static <T> T readBody(WebScriptRequest req, TypeReference<T> type) throws IOException {
        return readBody(req, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * Reads the required JSON body like Spring's {@code @RequestBody}: 415 unless the content type is
     * {@code application/json} or {@code application/*+json} (a missing content type counted as
     * {@code application/octet-stream}), and 400 for a missing or unreadable body. The body is streamed into Jackson
     * as bytes, so Jackson detects its encoding.
     */
    private static <T> T readBody(WebScriptRequest req, com.fasterxml.jackson.databind.JavaType type)
            throws IOException {
        requireJsonContentType(req.getContentType());

        Content content = req.getContent();
        T value;
        try (PushbackInputStream in = new PushbackInputStream(
                content == null ? InputStream.nullInputStream() : content.getInputStream())) {
            // Peek one byte to tell a missing body apart from an unreadable one, as Spring did.
            int first = in.read();
            if (first == -1) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required request body is missing");
            }
            in.unread(first);
            value = OBJECT_MAPPER.readValue(in, type);
        } catch (JsonProcessingException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid request body: " + e.getOriginalMessage());
        }
        if (value == null) {
            // A literal JSON null: Spring treated a required body that converts to null as missing.
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required request body is missing");
        }
        return value;
    }

    static void requireJsonContentType(String contentType) {
        MediaType mediaType;
        try {
            mediaType = contentType == null || contentType.isEmpty()
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException e) {
            throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE,
                    "Content-Type '" + contentType + "' is not supported.");
        }
        if (!MediaType.APPLICATION_JSON.includes(mediaType) && !JSON_SUBTYPES.includes(mediaType)) {
            throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE,
                    "Content-Type '" + mediaType + "' is not supported.");
        }
    }
}
