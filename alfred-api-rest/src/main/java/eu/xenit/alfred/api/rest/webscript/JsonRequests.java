package eu.xenit.alfred.api.rest.webscript;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Reads JSON request bodies the way Spring MVC's {@code @RequestBody} did before the migration.
 */
public final class JsonRequests {

    // The Jackson configuration MVC used for request bodies. Not modified after creation, so safe to share.
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getNewObjectMapper();

    private static final MediaType JSON_SUBTYPES = new MediaType("application", "*+json");

    private JsonRequests() {
    }

    public static <T> T readBody(WebScriptRequest req, Class<T> type) throws IOException {
        return readBody(req, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    public static <T> T readBody(WebScriptRequest req, TypeReference<T> type) throws IOException {
        return readBody(req, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * Reads the required body: 415 unless the content type is {@code application/json} or {@code application/*+json}
     * (none counts as {@code application/octet-stream}), 400 if it is missing or unreadable. The body is streamed to
     * Jackson as bytes, so Jackson detects the encoding.
     */
    private static <T> T readBody(WebScriptRequest req, JavaType type) throws IOException {
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
