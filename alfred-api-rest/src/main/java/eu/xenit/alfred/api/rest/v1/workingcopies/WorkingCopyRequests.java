package eu.xenit.alfred.api.rest.v1.workingcopies;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Request and response handling shared by the working copy web scripts.
 */
final class WorkingCopyRequests {

    private WorkingCopyRequests() {
    }

    /** Builds the node reference from the {space}/{store}/{guid} template variables. */
    static NodeRef nodeRef(WebScriptRequest req) {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        return new NodeRef(vars.get("space"), vars.get("store"), vars.get("guid"));
    }

    /** The 404 response of the MVC implementation: a plain-text message. */
    static void writeDoesNotExist(WebScriptResponse res, NodeRef nodeRef) throws IOException {
        JsonResponses.writeText(res, Status.STATUS_NOT_FOUND, String.format("%s does not exist.", nodeRef));
    }
}