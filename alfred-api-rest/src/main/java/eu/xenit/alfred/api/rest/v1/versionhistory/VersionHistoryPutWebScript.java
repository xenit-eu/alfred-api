package eu.xenit.alfred.api.rest.v1.versionhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.versionhistory.put")
public class VersionHistoryPutWebScript extends AbstractWebScript {

    private static final QName PROP_INITIAL_VERSION =
            new QName("{http://www.alfresco.org/model/content/1.0}initialVersion");
    private static final QName PROP_AUTO_VERSION =
            new QName("{http://www.alfresco.org/model/content/1.0}autoVersion");
    private static final QName PROP_AUTO_VERSION_PROPS =
            new QName("{http://www.alfresco.org/model/content/1.0}autoVersionOnUpdateProps");

    private final IVersionHistoryService versionHistoryService;
    private final ObjectMapper objectMapper;

    public VersionHistoryPutWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
        this.objectMapper = ObjectMapperFactory.getNewObjectMapper();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        VersionOptions versionOptions = readVersionOptions(req);

        HashMap<QName, Serializable> versionProperties = new HashMap<>();
        if (versionOptions != null) {
            if (versionOptions.getAutoVersion() != null) {
                versionProperties.put(PROP_AUTO_VERSION, versionOptions.getAutoVersion());
            }
            if (versionOptions.getAutoVersionOnUpdateProps() != null) {
                versionProperties.put(PROP_AUTO_VERSION_PROPS, versionOptions.getAutoVersionOnUpdateProps());
            }
            if (versionOptions.getInitialVersion() != null) {
                versionProperties.put(PROP_INITIAL_VERSION, versionOptions.getInitialVersion());
            }
        }
        versionHistoryService.ensureVersioningEnabled(VersionHistoryRequests.nodeRef(req), versionProperties);
        JsonResponses.writeEmpty(res);
    }

    /** The body is optional, like {@code @RequestBody(required = false)} in the MVC implementation. */
    private VersionOptions readVersionOptions(WebScriptRequest req) throws IOException {
        Content content = req.getContent();
        String body = content == null ? null : content.getContent();
        if (body == null || body.isBlank()) {
            return null;
        }
        return objectMapper.readValue(body, VersionOptions.class);
    }
}