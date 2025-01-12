package eu.xenit.alfred.api.rest.v0.metadata;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.permissions.IPermissionService;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetadataBulkWebscript {

    private final INodeService service;
    private final IPermissionService permissionService;

    public MetadataBulkWebscript(INodeService service, IPermissionService permissionService) {
        this.service = service;
        this.permissionService = permissionService;
    }

    @AlfrescoTransaction
    @PostMapping(
            value = "/eu/xenit/metadata/bulk",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<NodeMetadataV0>> execute(@RequestBody final List<NodeRef> nodeRefs) {
        List<NodeMetadataV0> metadatas = new ArrayList<>();
        for (NodeRef el : nodeRefs) {
            metadatas.add(NodeMetadataV0.FromV1(service.getMetadata(el), permissionService));
        }
        return ResponseEntity.ok(metadatas);
    }
}
