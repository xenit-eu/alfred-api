package eu.xenit.alfred.api.rest.webscript;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.extensions.webscripts.WebScriptResponse;

class TextResponsesTest {

    @Test
    void writesUtf8TextAndDeclaresUtf8() throws IOException {
        String message = "User Україна – Empfänger does not exist";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);

        TextResponses.writeText(res, 404, message);

        verify(res).setStatus(404);
        verify(res).setContentType("text/plain;charset=UTF-8");
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).getWriter();
        // The order of the headers does not matter, but each must be set before the body: on a direct (unbuffered)
        // response, headers set after the body has been committed are ignored.
        assertSetBeforeBody(res, r -> r.setStatus(404));
        assertSetBeforeBody(res, r -> r.setContentType("text/plain;charset=UTF-8"));
        assertSetBeforeBody(res, r -> r.setHeader("Cache-Control", "no-cache"));
        assertArrayEquals(message.getBytes(StandardCharsets.UTF_8), out.toByteArray());
    }

    @Test
    void writesAnEmpty200WithoutContentType() throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);

        TextResponses.writeEmpty(res);

        verify(res).setStatus(200);
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).setContentType(anyString());
        verify(res, never()).getOutputStream();
        verify(res, never()).getWriter();
    }

    private static void assertSetBeforeBody(WebScriptResponse res, Consumer<WebScriptResponse> header)
            throws IOException {
        InOrder order = inOrder(res);
        header.accept(order.verify(res));
        order.verify(res).getOutputStream();
    }
}
