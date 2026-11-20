package eu.xenit.alfred.api.rest.v0.metadata;

import eu.xenit.alfred.api.data.NodeRef;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Request handling specific to the v0 metadata web scripts, reproducing how Spring MVC bound
 * {@code @RequestParam NodeRef nodeRef} in the original implementation. Request bodies are read with
 * {@link eu.xenit.alfred.api.rest.webscript.JsonRequests}.
 */
final class MetadataRequests {

    private MetadataRequests() {
    }

    /**
     * The required {@code nodeRef} query parameter. A missing parameter is a 400; any value that is present (even
     * empty or malformed) is passed on, so the service rejects it exactly as before.
     */
    static NodeRef nodeRef(WebScriptRequest req) {
        String value = req.getParameter("nodeRef");
        if (value == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required parameter 'nodeRef' is not present.");
        }
        return new NodeRef(value);
    }
}
