package eu.xenit.alfred.api.rest.webscript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Plain-text and empty web script responses, with the {@code Cache-Control: no-cache} header of all Alfred API
 * responses.
 */
public final class TextResponses {

    private TextResponses() {
    }

    /**
     * Writes a plain-text body, such as an error message. UTF-8 is both declared in the Content-Type and used to encode
     * the bytes (deliberately: MVC used ISO-8859-1, which cannot represent all characters).
     */
    public static void writeText(WebScriptResponse res, int status, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        res.setStatus(status);
        res.setContentType("text/plain;charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache");
        // The output stream is owned by the web script framework: do not flush or close it here.
        res.getOutputStream().write(body);
    }

    /** Writes a 200 response without a body or Content-Type. */
    public static void writeEmpty(WebScriptResponse res) {
        res.setStatus(Status.STATUS_OK);
        res.setHeader("Cache-Control", "no-cache");
    }
}
