package eu.xenit.alfred.api.rest.v1;


import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.filefolder.NodePath;
import eu.xenit.alfred.api.node.ChildParentAssociation;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.NodeAssociation;
import eu.xenit.alfred.api.node.NodeAssociations;
import eu.xenit.alfred.api.node.NodeMetadata;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.permissions.PermissionValue;
import eu.xenit.alfred.api.rest.v1.nodes.NodeInfo;
import eu.xenit.alfred.api.rest.v1.nodes.NodeInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlfredApiV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(AlfredApiV1Webscript.class);

    protected <T> ResponseEntity<T> writeJsonResponse(T object) {
        return ResponseEntity.ok(object);
    }

    protected NodeRef createNodeRef(String space, String store, String guid) {
        return new NodeRef(space, store, guid);
    }

    public static List<NodeInfo> nodeRefToNodeInfo(NodeInfoRequest nodeInfoRequest,
            IFileFolderService fileFolderService,
            INodeService nodeService,
            IPermissionService permissionService
    ) {
        List<NodeInfo> nodeInfoList = new ArrayList<>();
        List<NodeRef> nodeRefs = nodeInfoRequest.getNoderefs().stream().map(NodeRef::new).toList();
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

    public static NodeInfo nodeRefToNodeInfo(NodeRef nodeRef, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        return nodeRefToNodeInfo(nodeRef, fileFolderService, nodeService, permissionService,
                true, true, true, true,
                true, true, true, true);
    }

    public static NodeInfo nodeRefToNodeInfo(NodeRef nodeRef,
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

        NodePath path = null;
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
