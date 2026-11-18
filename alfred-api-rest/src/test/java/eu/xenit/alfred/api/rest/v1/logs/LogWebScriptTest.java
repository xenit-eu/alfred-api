package eu.xenit.alfred.api.rest.v1.logs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class LogWebScriptTest {

    private static final String ORIGINAL_LOG_PATH_EXPRESSION = "$CATALINA_HOME/logs/catalina.out";

    // These mirror Spring's @RequestParam(defaultValue = "200") int lines binding in the original MVC endpoint.

    @Test
    void missingOrEmptyLinesUsesTheDefault() {
        assertEquals(200, LogWebScript.parseLines(null));
        assertEquals(200, LogWebScript.parseLines(""));
    }

    @Test
    void linesAcceptsTheValuesSpringAccepted() {
        assertEquals(5, LogWebScript.parseLines("5"));
        assertEquals(5, LogWebScript.parseLines(" 5 "));
        assertEquals(16, LogWebScript.parseLines("0x10"));
    }

    @Test
    void linesRejectsTheValuesSpringRejectedWithBadRequest() {
        // Whitespace only is not empty, so Spring converted it (and failed) instead of applying the default.
        for (String invalid : new String[]{"abc", "  ", "99999999999"}) {
            WebScriptException e = assertThrows(WebScriptException.class, () -> LogWebScript.parseLines(invalid),
                    invalid);
            assertEquals(400, e.getStatus(), invalid);
        }
    }

    @Test
    void writesTheLastLinesNewestFirstAsIso88591PlainText(@TempDir Path dir) throws IOException {
        Path log = dir.resolve("catalina.out");
        Files.writeString(log, "first\nsecond é\nthird\n", StandardCharsets.UTF_8);
        Environment env = mock(Environment.class);
        when(env.resolvePlaceholders(ORIGINAL_LOG_PATH_EXPRESSION)).thenReturn(log.toString());
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("lines")).thenReturn("2");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);

        new LogWebScript(env).execute(req, res);

        verify(res).setStatus(200);
        verify(res).setContentType("text/plain;charset=ISO-8859-1");
        verify(res).setHeader("Cache-Control", "no-cache");
        assertEquals("third\nsecond é\n", out.toString(StandardCharsets.ISO_8859_1));
    }
}
