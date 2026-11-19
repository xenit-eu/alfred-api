package eu.xenit.alfred.api.rest.v0.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * POST /apix/eu/xenit/metadata/bulk: the v0 metadata of every node in the JSON array body, in request order.
 * <p>
 * This is the metadata-specific bulk read, unrelated to the general /apix/v1/bulk dispatcher.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.metadata.metadata-bulk.post")
public class NodeMetadataBulkWebScript extends AbstractWebScript {

    private static final TypeReference<List<NodeRef>> NODE_REFS = new TypeReference<>() {
    };

    private final INodeService nodeService;
    private final IPermissionService permissionService;

    public NodeMetadataBulkWebScript(INodeService nodeService, IPermissionService permissionService) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        List<NodeRef> nodeRefs = MetadataRequests.readBody(req, NODE_REFS);
        List<NodeMetadataV0> metadatas = new ArrayList<>();
        for (NodeRef nodeRef : nodeRefs) {
            metadatas.add(NodeMetadataV0.FromV1(nodeService.getMetadata(nodeRef), permissionService));
        }
        JsonResponses.writeJson(res, metadatas);
    }
}
