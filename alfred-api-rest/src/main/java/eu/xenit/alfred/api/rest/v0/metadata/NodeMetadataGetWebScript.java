package eu.xenit.alfred.api.rest.v0.metadata;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/eu/xenit/metadata?nodeRef=...: the v0 metadata of a node.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.metadata.metadata.get")
public class NodeMetadataGetWebScript extends AbstractWebScript {

    private final INodeService nodeService;
    private final IPermissionService permissionService;

    public NodeMetadataGetWebScript(INodeService nodeService, IPermissionService permissionService) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = MetadataRequests.nodeRef(req);
        JsonResponses.writeJson(res, NodeMetadataV0.FromV1(nodeService.getMetadata(nodeRef), permissionService));
    }
}
