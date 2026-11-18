package eu.xenit.alfred.api.rest.webscript;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import java.io.IOException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Writes JSON responses from native web scripts the same way the original Alfresco MVC implementation did:
 * UTF-8 body, {@code Content-Type: application/json} (no charset parameter) and {@code Cache-Control: no-cache}.
 */
public final class JsonResponses {

    // Fully configured by the factory and never modified afterwards, so it is safe to share between threads.
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getNewObjectMapper();

    private JsonResponses() {
    }

    public static void writeJson(WebScriptResponse res, Object body) throws IOException {
        writeJson(res, Status.STATUS_OK, body);
    }

    /**
     * Writes a 200 response without a body, like Alfresco MVC did for {@code ResponseEntity.ok().build()} and for a
     * {@code null} response body.
     */
    public static void writeEmpty(WebScriptResponse res) {
        res.setStatus(Status.STATUS_OK);
        res.setHeader("Cache-Control", "no-cache");
    }

    public static void writeJson(WebScriptResponse res, int status, Object body) throws IOException {
        // Serialize first, so nothing is written to the response if serialization fails. Jackson produces UTF-8
        // bytes; writing bytes avoids the servlet writer, whose default charset is container-dependent.
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(body);

        res.setStatus(status);
        res.setContentType("application/json");
        // Reproduces the header the Alfresco MVC dispatcher added to every response.
        res.setHeader("Cache-Control", "no-cache");
        // The output stream is owned by the web script framework: do not flush or close it here.
        res.getOutputStream().write(bytes);
    }
}
