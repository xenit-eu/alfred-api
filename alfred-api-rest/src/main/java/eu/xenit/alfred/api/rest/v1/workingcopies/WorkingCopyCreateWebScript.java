package eu.xenit.alfred.api.rest.v1.workingcopies;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.workingcopies.workingcopies.post")
public class WorkingCopyCreateWebScript extends AbstractWebScript {

    private final INodeService nodeService;

    public WorkingCopyCreateWebScript(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        CheckoutBody checkoutBody = WorkingCopyRequests.readBody(req, CheckoutBody.class);
        NodeRef originalRef = checkoutBody.getOriginal();
        NodeRef destinationRef = checkoutBody.getDestinationFolder();

        if (!nodeService.exists(originalRef)) {
            WorkingCopyRequests.writeDoesNotExist(res, originalRef);
            return;
        }
        // A destination folder is optional, but when one is specified it must exist.
        if (destinationRef != null && !nodeService.exists(destinationRef)) {
            WorkingCopyRequests.writeDoesNotExist(res, destinationRef);
            return;
        }

        NodeRef workingCopyRef = nodeService.checkout(originalRef, destinationRef);
        JsonResponses.writeJson(res, new NoderefResult(workingCopyRef));
    }
}