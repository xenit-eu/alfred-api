package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.data.NodeRef;
import java.util.Map;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Reads the template variables shared by the version history web scripts.
 */
final class VersionHistoryRequests {

    private VersionHistoryRequests() {
    }

    /** Builds the node reference from the {space}/{store}/{guid} template variables. */
    static NodeRef nodeRef(WebScriptRequest req) {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        return new NodeRef(vars.get("space"), vars.get("store"), vars.get("guid"));
    }

    /** Returns the {label} template variable. */
    static String label(WebScriptRequest req) {
        return req.getServiceMatch().getTemplateVars().get("label");
    }
}