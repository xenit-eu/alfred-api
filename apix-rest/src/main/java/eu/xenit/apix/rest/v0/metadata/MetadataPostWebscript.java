package eu.xenit.apix.rest.v0.metadata;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetadataPostWebscript {

    private final INodeService service;
    private final IPermissionService permissionService;

    public MetadataPostWebscript(INodeService service, IPermissionService permissionService) {
        this.service = service;
        this.permissionService = permissionService;
    }

    @AlfrescoTransaction
    @PostMapping(
            value = "/eu/xenit/metadata",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NodeMetadataV0> execute(@RequestParam final NodeRef nodeRef,
            @RequestBody final MetadataChangesV0 changes) {
        return ResponseEntity.ok(
                NodeMetadataV0.FromV1(
                        service.setMetadata(nodeRef, changes.ToV1()), permissionService
                )
        );
    }
}
