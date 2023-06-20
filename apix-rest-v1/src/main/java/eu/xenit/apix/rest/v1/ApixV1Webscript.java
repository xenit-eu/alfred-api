package eu.xenit.apix.rest.v1;


import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeAssociation;
import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.rest.v1.nodes.NodeInfoRequest;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(ApixV1Webscript.class);

    protected <T> ResponseEntity<T> writeJsonResponse(T object) {
        return ResponseEntity.ok(object);
    }

    protected NodeRef createNodeRef(String space, String store, String guid) {
        return new NodeRef(space, store, guid);
    }

    protected List<NodeInfo> nodeRefToNodeInfo(NodeInfoRequest nodeInfoRequest,
                                               IFileFolderService fileFolderService,
                                               INodeService nodeService,
                                               IPermissionService permissionService
    ) {
        List<NodeInfo> nodeInfoList = new ArrayList<>();
        List<NodeRef> nodeRefs = nodeInfoRequest.getNoderefs().stream().map(NodeRef::new).collect(Collectors.toList());
        for (NodeRef nodeRef : nodeRefs) {
            NodeInfo nodeInfo = nodeRefToNodeInfo(nodeRef,
                    fileFolderService,
                    nodeService,
                    permissionService,
                    nodeInfoRequest.getRetrievePath(),
                    nodeInfoRequest.getRetrieveMetadata(),
                    nodeInfoRequest.getRetrievePermissions(),
                    nodeInfoRequest.getRetrieveAssocs(),
                    nodeInfoRequest.getRetrieveChildAssocs(),
                    nodeInfoRequest.getRetrieveParentAssocs(),
                    nodeInfoRequest.getRetrieveTargetAssocs(),
                    nodeInfoRequest.getRetrieveSourceAssocs());
            if (nodeInfo == null) {
                continue;
            }

            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
    }

    protected NodeInfo nodeRefToNodeInfo(NodeRef nodeRef, IFileFolderService fileFolderService,
                                         INodeService nodeService, IPermissionService permissionService) {
        return nodeRefToNodeInfo(nodeRef, fileFolderService, nodeService, permissionService,
                true, true, true, true,
                true, true, true, true);
    }

    protected NodeInfo nodeRefToNodeInfo(NodeRef nodeRef,
                                         IFileFolderService fileFolderService,
                                         INodeService nodeService,
                                         IPermissionService permissionService,
                                         boolean retrievePath,
                                         boolean retrieveMetadata,
                                         boolean retrievePermissions,
                                         boolean retrieveAssocs,
                                         boolean retrieveChildAssocs,
                                         boolean retrieveParentAssocs,
                                         boolean retrieveTargetAssocs,
                                         boolean retrieveSourceAssocs) {
        if (!permissionService.hasPermission(nodeRef, IPermissionService.READ)) {
            logger.warn("Excluding node {} from results due to insufficient permissions", nodeRef);
            return null;
        }

        if (!nodeService.exists(nodeRef)) {
            logger.debug("Excluding node {} from results because it does not exist", nodeRef);
            return null;
        }

        eu.xenit.apix.filefolder.NodePath path = null;
        if (retrievePath) {
            path = fileFolderService.getPath(nodeRef);
        }

        NodeMetadata nodeMetadata = null;
        if (retrieveMetadata) {
            nodeMetadata = nodeService.getMetadata(nodeRef);
        }

        Map<String, PermissionValue> permissions = null;
        if (retrievePermissions) {
            permissions = permissionService.getPermissions(nodeRef);
        }

        NodeAssociations associations = null;
        if (retrieveAssocs) {
            List<ChildParentAssociation> childAssocs = null;
            if (retrieveChildAssocs) {
                childAssocs = nodeService.getChildAssociations(nodeRef);
            }
            List<ChildParentAssociation> parentAssociations = null;
            if (retrieveParentAssocs) {
                parentAssociations = nodeService.getParentAssociations(nodeRef);
            }
            List<NodeAssociation> targetAssociations = null;
            if (retrieveTargetAssocs) {
                targetAssociations = nodeService.getTargetAssociations(nodeRef);
            }
            List<NodeAssociation> sourceAssociationts = null;
            if (retrieveSourceAssocs) {
                sourceAssociationts = nodeService.getSourceAssociations(nodeRef);
            }
            associations = new NodeAssociations(childAssocs, parentAssociations, targetAssociations,
                    sourceAssociationts);
        }

        return new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
    }
}
