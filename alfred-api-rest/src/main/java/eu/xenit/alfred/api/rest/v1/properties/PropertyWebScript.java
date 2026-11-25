package eu.xenit.alfred.api.rest.v1.properties;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.properties.IPropertyService;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Returns the definition of a property.
 *
 * @deprecated since Oct 2015, use {@code /apix/v1/dictionary/properties/{qname}} instead
 */
@Deprecated(since = "Deprecated since Oct 2015, use /apix/v1/dictionary/properties/{qname} instead")
@Component("webscript.eu.xenit.alfred.api.rest.v1.properties.property.get")
public class PropertyWebScript extends AbstractWebScript {

    private final IPropertyService propertyService;

    public PropertyWebScript(IPropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        PropertyDefinition propDef = propertyService.GetPropertyDefinition(qnameFrom(req));
        if (propDef == null) {
            // Like ResponseEntity.notFound().build(): no body and no content type.
            res.setStatus(Status.STATUS_NOT_FOUND);
            res.setHeader("Cache-Control", "no-cache");
            return;
        }
        JsonResponses.writeJson(res, propDef);
    }

    /**
     * The {@code qnameWithSlash} parameter takes precedence over the path, as in the MVC implementation, where it was
     * the only way to pass a full QName. Web script URI variables may span '/', so a full QName in the path works too.
     */
    private static QName qnameFrom(WebScriptRequest req) {
        String qnameWithSlash = req.getParameter("qnameWithSlash");
        if (qnameWithSlash != null && !qnameWithSlash.isEmpty()) {
            return new QName(qnameWithSlash);
        }
        return new QName(req.getServiceMatch().getTemplateVars().get("qname"));
    }
}