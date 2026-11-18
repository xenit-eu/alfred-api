package eu.xenit.alfred.api.rest.v1.dictionary;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Behaviour shared by the dictionary definition lookups (properties, types and aspects by QName).
 */
final class DictionaryResponses {

    private DictionaryResponses() {
    }

    /**
     * The QName is everything after the endpoint prefix. Web script URI variables may span '/', so this also accepts
     * full QNames such as {@code {http://www.alfresco.org/model/content/1.0}name}, as the MVC "/**" mappings did.
     */
    static QName qnameFrom(WebScriptRequest req) {
        return new QName(req.getServiceMatch().getTemplateVars().get("qname"));
    }

    /**
     * Writes the definition as JSON, or an empty 404 when it does not exist, matching the original
     * {@code ResponseEntity.notFound().build()} (no body, no content type) plus the MVC dispatcher's cache header.
     */
    static void writeDefinitionOrNotFound(WebScriptResponse res, Object definition) throws IOException {
        if (definition == null) {
            res.setStatus(Status.STATUS_NOT_FOUND);
            res.setHeader("Cache-Control", "no-cache");
            return;
        }
        JsonResponses.writeJson(res, definition);
    }
}
