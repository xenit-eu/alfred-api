package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import eu.xenit.alfred.api.versionhistory.VersionHistory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.versions.get")
public class VersionHistoryGetWebScript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(VersionHistoryGetWebScript.class);

    private final IVersionHistoryService versionHistoryService;

    public VersionHistoryGetWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = VersionHistoryRequests.nodeRef(req);
        logger.debug("Asked versionhistory for node with guid: {}", nodeRef.getGuid());

        VersionHistory history = versionHistoryService.GetVersionHistory(nodeRef);
        if (history == null) {
            // Unversioned node: the MVC implementation returned 200 with an empty body.
            JsonResponses.writeEmpty(res);
            return;
        }
        JsonResponses.writeJson(res, history);
    }
}