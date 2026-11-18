package eu.xenit.alfred.api.rest.v1.workingcopies;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.workingcopies.checkin.post")
public class WorkingCopyCheckinWebScript extends AbstractWebScript {

    private final INodeService nodeService;

    public WorkingCopyCheckinWebScript(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef workingCopyRef = WorkingCopyRequests.nodeRef(req);
        CheckinBody checkinBody = WorkingCopyRequests.readBody(req, CheckinBody.class);

        if (!nodeService.exists(workingCopyRef)) {
            WorkingCopyRequests.writeDoesNotExist(res, workingCopyRef);
            return;
        }

        NodeRef originalRef = nodeService.checkin(workingCopyRef, checkinBody.getComment(),
                checkinBody.getMajorVersion());
        JsonResponses.writeJson(res, new NoderefResult(originalRef));
    }
}