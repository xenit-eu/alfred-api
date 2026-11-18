package eu.xenit.alfred.api.rest.v1.logs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.env.Environment;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;

/**
 * GET /apix/v1/tmp/log: returns the last lines of the Tomcat log as plain text. Admin only (see descriptor).
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.logs.log.get")
public class LogWebScript extends AbstractWebScript {

    static final int DEFAULT_LINES = 200;

    private final String logPath;

    public LogWebScript(Environment env) {
        // Kept exactly as in the original MVC implementation. Note that "$CATALINA_HOME" is not Spring placeholder
        // syntax ("${...}"), so it is not resolved and the path is used literally.
        logPath = env.resolvePlaceholders("$CATALINA_HOME/logs/catalina.out");
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        int lines = parseLines(req.getParameter("lines"));

        StringBuilder log = new StringBuilder();
        File logFile = new File(logPath);
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile)) {
            for (int i = 0; i < lines; i++) {
                log.append(reader.readLine())
                        .append("\n");
            }
        }

        // The MVC implementation returned a String through Spring's StringHttpMessageConverter, which writes
        // ISO-8859-1 and declares it in the Content-Type.
        byte[] body = log.toString().getBytes(StandardCharsets.ISO_8859_1);
        res.setStatus(Status.STATUS_OK);
        res.setContentType("text/plain;charset=ISO-8859-1");
        res.setHeader("Cache-Control", "no-cache");
        res.getOutputStream().write(body);
    }

    /**
     * Mirrors {@code @RequestParam(defaultValue = "200") int lines}: a missing or empty value uses the default.
     * Otherwise Spring converted the value with {@link NumberUtils#parseNumber}, which removes all whitespace and also
     * accepts hexadecimal ("0x10", "#10"); anything it rejects was a bad request.
     */
    static int parseLines(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT_LINES;
        }
        try {
            return NumberUtils.parseNumber(value, Integer.class);
        } catch (NumberFormatException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter 'lines' must be an integer: " + value);
        }
    }
}
