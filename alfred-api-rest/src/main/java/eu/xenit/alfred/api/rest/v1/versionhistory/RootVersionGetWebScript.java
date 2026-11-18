package eu.xenit.alfred.api.rest.v1.versionhistory;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.versionhistory.root.get")
public class RootVersionGetWebScript extends AbstractWebScript {

    private final IVersionHistoryService versionHistoryService;

    public RootVersionGetWebScript(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        JsonResponses.writeJson(res, versionHistoryService.getRootVersion(VersionHistoryRequests.nodeRef(req)));
    }
}