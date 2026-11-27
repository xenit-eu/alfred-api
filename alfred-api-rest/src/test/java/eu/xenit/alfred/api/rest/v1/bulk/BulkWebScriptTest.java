package eu.xenit.alfred.api.rest.v1.bulk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import eu.xenit.alfred.api.rest.v1.bulk.response.IntermediateResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class BulkWebScriptTest {

    @Test
    void aJsonBodyIsReturnedAsJsonWithTheFirstValueOfEachHeader() throws IOException {
        IntermediateResponse response = utf8Response();
        response.setStatus(200);
        response.setContentType("application/json");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("X-Test", "first");
        response.addHeader("X-Test", "second");
        response.getOutputStream().write("{\"name\":\"Україна\"}".getBytes(StandardCharsets.UTF_8));

        BulkSubResult result = BulkWebScript.toBulkSubResult(response);

        assertEquals(200, result.getStatusCode());
        assertEquals("Україна", ((JsonNode) result.getBody()).get("name").asText());
        // The response is decoded as UTF-8, which is also added to the reported Content-Type, as before.
        assertEquals(Map.of("Content-Type", "application/json;charset=UTF-8", "Cache-Control", "no-cache",
                "X-Test", "first"), result.getHeaders());
    }

    @Test
    void aBodyThatIsNotJsonIsReturnedAsAString() throws IOException {
        IntermediateResponse response = utf8Response();
        response.setStatus(404);
        response.setContentType("text/plain;charset=UTF-8");
        response.getOutputStream().write("User Україна does not exist".getBytes(StandardCharsets.UTF_8));

        BulkSubResult result = BulkWebScript.toBulkSubResult(response);

        assertEquals(404, result.getStatusCode());
        assertEquals("User Україна does not exist", result.getBody());
    }

    @Test
    void anEmptyBodyIsReturnedAsAnEmptyObject() throws IOException {
        IntermediateResponse response = utf8Response();
        response.setStatus(200);

        BulkSubResult result = BulkWebScript.toBulkSubResult(response);

        assertTrue(((JsonNode) result.getBody()).isObject());
        assertEquals(0, ((JsonNode) result.getBody()).size());
        assertTrue(result.getHeaders().isEmpty());
    }

    @Test
    void anEmptyBulkRequestIsA204WithoutBody() throws IOException {
        RuntimeContainer container = mock(RuntimeContainer.class);
        WebScriptRequest req = mock(WebScriptRequest.class);
        Content content = mock(Content.class);
        when(req.getContentType()).thenReturn("application/json");
        when(req.getContent()).thenReturn(content);
        when(content.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8)));
        WebScriptResponse res = mock(WebScriptResponse.class);

        new BulkWebScript(container).execute(req, res);

        verify(res).setStatus(204);
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).getOutputStream();
        verifyNoInteractions(container);
    }

    private static IntermediateResponse utf8Response() {
        IntermediateResponse response = new IntermediateResponse();
        response.setCharacterEncoding("UTF-8");
        return response;
    }
}
