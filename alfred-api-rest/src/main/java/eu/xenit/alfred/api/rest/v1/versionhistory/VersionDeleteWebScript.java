package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.version.delete")
public class VersionDeleteWebScript extends AbstractWebScript {

    private final IVersionHistoryService versionHistoryService;

    public VersionDeleteWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {
        versionHistoryService.deleteVersion(VersionHistoryRequests.nodeRef(req), VersionHistoryRequests.label(req));
        JsonResponses.writeEmpty(res);
    }
}