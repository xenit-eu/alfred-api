package eu.xenit.apix.rest.v2;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.*;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jasper on 16/02/17.
 */
public class ApixV2Webscript extends ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(ApixV2Webscript.class);

    protected List<NodeInfo> nodeRefToNodeInfo(List<NodeRef> nodeRefs, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        for (NodeRef nodeRef : nodeRefs) {
            eu.xenit.apix.filefolder.NodePath path = fileFolderService.getPath(nodeRef);
            NodeMetadata nodeMetadata = nodeService.getMetadata(nodeRef);
            Map<String, PermissionValue> permissions = permissionService.getPermissionsFast(nodeRef);
            NodeAssociations associations = nodeService.getAssociations(nodeRef);
            NodeInfo nodeInfo = new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
    }

    protected List<NodeInfo> nodeRefToNodeInfo(List<NodeRef> nodeRefs, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService,
            boolean retrievePath, boolean retrieveMetadata,
            boolean retrievePermissions, boolean retrieveAssocs,
            boolean retrieveChildAssocs, boolean retrieveParentAssocs,
            boolean retrieveTargetAssocs) {
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        for (NodeRef nodeRef : nodeRefs) {
            logger.debug("######################################################");

            logger.debug("start getPath");
            eu.xenit.apix.filefolder.NodePath path = null;
            if (retrievePath) {
                path = fileFolderService.getPath(nodeRef);
            }
            logger.debug("end getPath");

            logger.debug("start getMetadata");
            NodeMetadata nodeMetadata = null;
            if (retrieveMetadata) {
                nodeMetadata = nodeService.getMetadata(nodeRef);
            }
            logger.debug("end getMetadata");

            logger.debug("start getPermissions");
            Map<String, PermissionValue> permissions = null;
            if (retrievePermissions) {
                permissions = permissionService.getPermissionsFast(nodeRef);
            }
            logger.debug("end getPermissions");

            logger.debug("start getAssociations");
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
                associations = new NodeAssociations(childAssocs, parentAssociations, targetAssociations);
            }
            logger.debug("end getAssociations");

            logger.debug("start new NodeInfo");
            NodeInfo nodeInfo = new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
            logger.debug("end new NodeInfo");

            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
    }
}
