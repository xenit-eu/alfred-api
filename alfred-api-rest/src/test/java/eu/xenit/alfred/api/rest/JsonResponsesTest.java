package eu.xenit.alfred.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.extensions.webscripts.WebScriptResponse;

class JsonResponsesTest {

    private static final String NON_ASCII = "Alle Empfänger – Україна";

    @Test
    void defaultOverloadWritesUtf8JsonWithStatus200() throws IOException {
        TrackingOutputStream out = new TrackingOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        JsonResponses.writeJson(res, Map.of("name", NON_ASCII));

        verifyHeadersThenBody(res, 200);
        assertEquals("{\"name\":\"" + NON_ASCII + "\"}", out.toString(StandardCharsets.UTF_8));
        assertStreamLeftOpen(out);
    }

    @Test
    void explicitStatusOverloadWritesUtf8JsonWithGivenStatus() throws IOException {
        TrackingOutputStream out = new TrackingOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        JsonResponses.writeJson(res, 404, Map.of("name", NON_ASCII));

        verifyHeadersThenBody(res, 404);
        assertEquals("{\"name\":\"" + NON_ASCII + "\"}", out.toString(StandardCharsets.UTF_8));
        assertStreamLeftOpen(out);
    }

    @Test
    void serializationFailureLeavesResponseUntouched() {
        WebScriptResponse res = mock(WebScriptResponse.class);

        assertThrows(IOException.class, () -> JsonResponses.writeJson(res, new Unserializable()));

        verifyNoInteractions(res);
    }

    private static WebScriptResponse responseWritingTo(TrackingOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }

    private static void verifyHeadersThenBody(WebScriptResponse res, int status) throws IOException {
        InOrder order = inOrder(res);
        order.verify(res).setStatus(status);
        order.verify(res).setContentType("application/json");
        order.verify(res).setHeader("Cache-Control", "no-cache");
        order.verify(res).getOutputStream();
        verify(res, never()).getWriter();
        verify(res, never()).setContentEncoding(org.mockito.ArgumentMatchers.anyString());
    }

    private static void assertStreamLeftOpen(TrackingOutputStream out) {
        assertFalse(out.flushed, "The framework-owned output stream must not be flushed");
        assertFalse(out.closed, "The framework-owned output stream must not be closed");
    }

    /** ByteArrayOutputStream's flush() and close() are no-ops, so record the calls explicitly. */
    private static final class TrackingOutputStream extends ByteArrayOutputStream {

        private boolean flushed;
        private boolean closed;

        @Override
        public void flush() {
            flushed = true;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /** Jackson wraps the getter's exception in a JsonMappingException, which is an IOException. */
    private static final class Unserializable {

        public String getValue() {
            throw new IllegalStateException("not serializable");
        }
    }
}
