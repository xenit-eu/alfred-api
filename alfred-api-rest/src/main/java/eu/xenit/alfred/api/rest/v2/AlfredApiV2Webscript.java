package eu.xenit.alfred.api.rest.v2;

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
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import eu.xenit.alfred.api.rest.v1.nodes.NodeInfo;
import eu.xenit.alfred.api.rest.v1.nodes.NodeInfoRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jasper on 16/02/17.
 */
public class AlfredApiV2Webscript extends AlfredApiV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(AlfredApiV2Webscript.class);

    protected List<NodeInfo> nodeRefToNodeInfo(NodeInfoRequest nodeInfoRequest, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        List<NodeRef> nodeRefs = nodeInfoRequest.getNoderefs().stream().map(NodeRef::new).collect(Collectors.toList());

        return nodeRefs.stream().filter(nodeRef -> {
                    boolean hasPermission = permissionService.hasPermission(nodeRef, IPermissionService.READ);
                    if (!hasPermission) {
                        logger.warn("Excluding node {} from results due to insufficient permissions", nodeRef);
                    }
                    return hasPermission;

                })
                .filter(nodeRef -> {
                    boolean exists = nodeService.exists(nodeRef);
                    if (!exists) {
                        logger.debug("Excluding node {} from results because it does not exist", nodeRef);
                    }
                    return exists;

                }).map(nodeRef -> {
                    logger.debug("######################################################");
                    logger.debug("start new NodeInfo");
                    NodeInfo nodeInfo = new NodeInfo(nodeRef,
                            getNodeMetadata(nodeInfoRequest, nodeService, nodeRef),
                            getNodePermissions(nodeInfoRequest, permissionService, nodeRef),
                            getNodeAssociations(nodeInfoRequest, nodeService, nodeRef)
                            , getNodePath(nodeInfoRequest, fileFolderService, nodeRef));
                    logger.debug("end new NodeInfo");
                    return nodeInfo;
                }).collect(Collectors.toList());
    }

    private Map<String, PermissionValue> getNodePermissions(NodeInfoRequest nodeInfoRequest,
            IPermissionService permissionService, NodeRef nodeRef) {
        logger.debug("start getPermissions");
        Map<String, PermissionValue> permissions = null;
        if (nodeInfoRequest.getRetrievePermissions()) {
            permissions = permissionService.getPermissionsFast(nodeRef);
        }
        logger.debug("end getPermissions");
        return permissions;
    }

    private NodeMetadata getNodeMetadata(NodeInfoRequest nodeInfoRequest, INodeService nodeService, NodeRef nodeRef) {
        logger.debug("start getMetadata");
        NodeMetadata nodeMetadata = null;
        if (nodeInfoRequest.getRetrieveMetadata()) {
            nodeMetadata = nodeService.getMetadata(nodeRef);
        }
        logger.debug("end getMetadata");
        return nodeMetadata;
    }

    private NodePath getNodePath(NodeInfoRequest nodeInfoRequest, IFileFolderService fileFolderService,
            NodeRef nodeRef) {
        logger.debug("start getPath");
        NodePath path = null;
        if (nodeInfoRequest.getRetrievePath()) {
            path = fileFolderService.getPath(nodeRef);
        }
        logger.debug("end getPath");
        return path;
    }

    private NodeAssociations getNodeAssociations(NodeInfoRequest nodeInfoRequest, INodeService nodeService,
            NodeRef nodeRef) {
        logger.debug("start getAssociations");
        NodeAssociations associations = null;
        if (nodeInfoRequest.getRetrieveAssocs()) {
            List<ChildParentAssociation> childAssocs = null;
            if (nodeInfoRequest.getRetrieveChildAssocs()) {
                childAssocs = nodeService.getChildAssociations(nodeRef);
            }
            List<ChildParentAssociation> parentAssociations = null;
            if (nodeInfoRequest.getRetrieveParentAssocs()) {
                parentAssociations = nodeService.getParentAssociations(nodeRef);
            }
            List<NodeAssociation> targetAssociations = null;
            if (nodeInfoRequest.getRetrieveTargetAssocs()) {
                targetAssociations = nodeService.getTargetAssociations(nodeRef);
            }
            List<NodeAssociation> sourceAssociations = null;
            if (nodeInfoRequest.getRetrieveSourceAssocs()) {
                sourceAssociations = nodeService.getSourceAssociations(nodeRef);
            }
            associations = new NodeAssociations(childAssocs, parentAssociations, targetAssociations,
                    sourceAssociations);
        }
        logger.debug("end getAssociations");
        return associations;
    }
}
