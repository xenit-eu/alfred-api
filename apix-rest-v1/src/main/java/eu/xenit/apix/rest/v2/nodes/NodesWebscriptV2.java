package eu.xenit.apix.rest.v2.nodes;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.nodes.CreateNodeOptions;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.rest.v1.nodes.NodeInfoRequest;
import eu.xenit.apix.rest.v2.ApixV2Webscript;
import org.alfresco.service.ServiceRegistry;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AlfrescoAuthentication
@RestController
public class NodesWebscriptV2 extends ApixV2Webscript {

    private static final Logger logger = LoggerFactory.getLogger(NodesWebscriptV2.class);

    private final INodeService nodeService;

    private final IPermissionService permissionService;

    private final IFileFolderService fileFolderService;

    private final ServiceRegistry serviceRegistry;

    public NodesWebscriptV2(INodeService nodeService, IPermissionService permissionService,
                            IFileFolderService fileFolderService, ServiceRegistry serviceRegistry) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.fileFolderService = fileFolderService;
        this.serviceRegistry = serviceRegistry;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/nodes/{space}/{store}/{guid}")
    public ResponseEntity<NodeInfo> getAllInfo(@PathVariable String space,
                                               @PathVariable String store,
                                               @PathVariable String guid) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(nodeRef, this.fileFolderService, this.nodeService, this.permissionService);

        return writeJsonResponse(nodeInfo);
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v2/nodes/nodeInfo")
    public ResponseEntity<List<NodeInfo>> getAllInfos(@RequestBody final NodeInfoRequest nodeInfoRequest) throws JSONException {
        List<NodeInfo> nodeInfoList = this.nodeRefToNodeInfo(
                nodeInfoRequest,
                this.fileFolderService,
                this.nodeService,
                this.permissionService);
        return writeJsonResponse(nodeInfoList);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/nodes/{space}/{store}/{guid}/permissions")
    public ResponseEntity<Map<String, PermissionValue>> getPermissions(@PathVariable String space,
                                                                       @PathVariable String store,
                                                                       @PathVariable String guid) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        return writeJsonResponse(
                this.permissionService.getPermissionsFast(nodeRef)
        );
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v2/nodes")
    public ResponseEntity<?> createNode(@RequestBody final CreateNodeOptions createNodeOptions) {
        final StringBuilder errorMessage = new StringBuilder();
        final AtomicInteger errorCode = new AtomicInteger();
        Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    NodeRef parent = new NodeRef(createNodeOptions.getParent());

                    if (!nodeService.exists(parent)) {
                        errorCode.addAndGet(HttpStatus.SC_NOT_FOUND);
                        errorMessage.append("Parent does not exist");
                        return null;
                    }

                    NodeRef nodeRef;
                    NodeRef copyFrom = null;
                    if (createNodeOptions.getCopyFrom() == null) {
                        nodeRef = nodeService
                                .createNode(parent, createNodeOptions.getName(),
                                        new QName(createNodeOptions.getType()));
                    } else {
                        copyFrom = new NodeRef(createNodeOptions.getCopyFrom());
                        if (!nodeService.exists(copyFrom)) {
                            errorCode.addAndGet(HttpStatus.SC_NOT_FOUND);
                            errorMessage.append("CopyFrom does not exist");
                            return null;
                        }
                        nodeRef = nodeService.copyNode(copyFrom, parent, true);
                    }

                    MetadataChanges metadataChanges;
                    QName type;
                    if (createNodeOptions.getType() != null) {
                        type = new QName(createNodeOptions.getType());
                    } else if (createNodeOptions.getCopyFrom() != null) {
                        type = nodeService.getMetadata(copyFrom).getType();
                    } else {
                        errorCode.addAndGet(HttpStatus.SC_BAD_REQUEST);
                        errorMessage.append(
                                "Please provide parameter \"type\" when creating a new node"
                        );
                        return null;
                    }
                    metadataChanges = new MetadataChanges(type, null, null,
                            createNodeOptions.getProperties());
                    nodeService.setMetadata(nodeRef, metadataChanges);

                    return nodeRef;
                }, false, true);

        if (resultObject == null) {
            return ResponseEntity.status(errorCode.get())
                    .body(errorMessage.toString());
        }

        NodeRef resultRef = new NodeRef(resultObject.toString());

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

        return writeJsonResponse(nodeInfo);
    }
}
