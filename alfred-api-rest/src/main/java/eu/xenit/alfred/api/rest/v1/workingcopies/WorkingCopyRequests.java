package eu.xenit.alfred.api.rest.v1.workingcopies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Request and response handling shared by the working copy web scripts.
 */
final class WorkingCopyRequests {

    // Fully configured by the factory and never modified afterwards, so it is safe to share between threads.
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getNewObjectMapper();

    private WorkingCopyRequests() {
    }

    /** Builds the node reference from the {space}/{store}/{guid} template variables. */
    static NodeRef nodeRef(WebScriptRequest req) {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        return new NodeRef(vars.get("space"), vars.get("store"), vars.get("guid"));
    }

    /**
     * Reads the required JSON body. A missing or unreadable body is answered with 400, like
     * {@code @RequestBody} in the MVC implementation.
     */
    static <T> T readBody(WebScriptRequest req, Class<T> type) throws IOException {
        Content content = req.getContent();
        String body = content == null ? null : content.getContent();
        if (body == null || body.isBlank()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required request body is missing");
        }
        try {
            return OBJECT_MAPPER.readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid request body: " + e.getOriginalMessage());
        }
    }

    /** The 404 response of the MVC implementation: a plain-text message. */
    static void writeDoesNotExist(WebScriptResponse res, NodeRef nodeRef) throws IOException {
        JsonResponses.writeText(res, Status.STATUS_NOT_FOUND, String.format("%s does not exist.", nodeRef));
    }
}