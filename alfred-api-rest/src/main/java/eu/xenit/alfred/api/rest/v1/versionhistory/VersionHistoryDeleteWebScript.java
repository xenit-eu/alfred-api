package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * There is no way to disable versioning: deleting merely resets the version history, and a new history starts with
 * the next version change.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.versionhistory.delete")
public class VersionHistoryDeleteWebScript extends AbstractWebScript {

    private final IVersionHistoryService versionHistoryService;

    public VersionHistoryDeleteWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {
        versionHistoryService.deleteVersionHistory(VersionHistoryRequests.nodeRef(req));
        JsonResponses.writeEmpty(res);
    }
}