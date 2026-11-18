package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.revert.post")
public class VersionRevertWebScript extends AbstractWebScript {

    private final IVersionHistoryService versionHistoryService;

    public VersionRevertWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {
        versionHistoryService.revert(VersionHistoryRequests.nodeRef(req), VersionHistoryRequests.label(req));
        JsonResponses.writeEmpty(res);
    }
}