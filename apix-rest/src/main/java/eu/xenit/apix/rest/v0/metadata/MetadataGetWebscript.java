package eu.xenit.apix.rest.v0.metadata;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetadataGetWebscript {

    private final INodeService service;
    private final IPermissionService permissionService;

    public MetadataGetWebscript(INodeService service, IPermissionService permissionService) {
        this.service = service;
        this.permissionService = permissionService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(
            value = "/eu/xenit/metadata",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NodeMetadataV0> execute(@RequestParam final NodeRef nodeRef) {
        return ResponseEntity.ok(
                NodeMetadataV0.FromV1(
                        service.getMetadata(nodeRef), permissionService
                )
        );
    }
}
