package eu.xenit.alfred.api.rest.webscript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

class JsonRequestsTest {

    private static final TypeReference<Map<String, List<String>>> BODY = new TypeReference<>() {
    };

    @Test
    void onlyJsonContentTypesAreAccepted() {
        JsonRequests.requireJsonContentType("application/json");
        JsonRequests.requireJsonContentType("application/json;charset=UTF-8");
        JsonRequests.requireJsonContentType("application/vnd.example+json");
        for (String unsupported : new String[]{null, "", "text/plain", "application/x-www-form-urlencoded", "%%%"}) {
            WebScriptException e = assertThrows(WebScriptException.class,
                    () -> JsonRequests.requireJsonContentType(unsupported), unsupported);
            assertEquals(415, e.getStatus(), unsupported);
        }
    }

    @Test
    void unsupportedContentTypeIsRejectedBeforeTheBodyIsRead() {
        WebScriptRequest req = request("text/plain", "{");
        assertEquals(415, assertThrows(WebScriptException.class, () -> JsonRequests.readBody(req, BODY)).getStatus());
    }

    @Test
    void missingOrUnreadableBodyIsBadRequest() {
        for (String body : new String[]{"", "{", "null"}) {
            WebScriptException e = assertThrows(WebScriptException.class,
                    () -> JsonRequests.readBody(request("application/json", body), BODY), body);
            assertEquals(400, e.getStatus(), body);
        }
    }

    @Test
    void bodyIsDecodedAsUtf8WithTheSharedJacksonConfiguration() throws IOException {
        // Unknown properties are ignored and a single value is accepted as a one-element array.
        WebScriptRequest req = request("application/json", "{\"users\":\"Україна\",\"unknown\":[]}");

        Map<String, List<String>> body = JsonRequests.readBody(req, BODY);

        assertEquals(List.of("Україна"), body.get("users"));
    }

    private static WebScriptRequest request(String contentType, String body) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        Content content = mock(Content.class);
        try {
            when(content.getInputStream()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        when(req.getContentType()).thenReturn(contentType);
        when(req.getContent()).thenReturn(content);
        return req;
    }
}
