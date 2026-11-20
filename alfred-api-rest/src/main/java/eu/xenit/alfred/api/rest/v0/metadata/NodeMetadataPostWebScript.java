package eu.xenit.alfred.api.rest.v0.metadata;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.rest.webscript.JsonRequests;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * POST /apix/eu/xenit/metadata?nodeRef=...: applies v0 metadata changes and returns the updated v0 metadata.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.metadata.metadata.post")
public class NodeMetadataPostWebScript extends AbstractWebScript {

    private final INodeService nodeService;
    private final IPermissionService permissionService;

    public NodeMetadataPostWebScript(INodeService nodeService, IPermissionService permissionService) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // Same binding order as the MVC method: the nodeRef parameter, then the body.
        NodeRef nodeRef = MetadataRequests.nodeRef(req);
        MetadataChangesV0 changes = JsonRequests.readBody(req, MetadataChangesV0.class);
        JsonResponses.writeJson(res,
                NodeMetadataV0.FromV1(nodeService.setMetadata(nodeRef, changes.ToV1()), permissionService));
    }
}
