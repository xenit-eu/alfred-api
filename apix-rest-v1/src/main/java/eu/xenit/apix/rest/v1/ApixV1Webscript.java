package eu.xenit.apix.rest.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import eu.xenit.apix.rest.v1.nodes.NodeInfoFlags;
import eu.xenit.apix.rest.v1.nodes.NodeInfoOptions;
import org.alfresco.repo.admin.SysAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Created by kenneth on 14.03.16.
 */
public class ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(ApixV1Webscript.class);

    @Autowired
    SysAdminParams sysAdminParams;

    protected void writeJsonResponse(WebScriptResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setContentEncoding("utf-8");
        response.setHeader("Cache-Control", "no-cache");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new ISO8601DateFormat());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(response.getWriter(), object);
    }

    protected NodeRef createNodeRef(String space, String store, String guid) {
        return new NodeRef(space, store, guid);
    }

    public String removeEscapeCharacters(String str) {
        return str.replace("\\", "");
    }

    protected List<NodeInfo> nodeRefToNodeInfo(
            IFileFolderService fileFolderService,
            INodeService nodeService,
            IPermissionService permissionService,
            NodeInfoOptions nodeInfoOptions
    ) {
        List<NodeInfo> nodeInfoList = new ArrayList<>();
        for (NodeRef nodeRef : nodeInfoOptions.getNodeReferences()) {
            NodeInfo nodeInfo = nodeRefToNodeInfo(nodeRef, fileFolderService,
                    nodeService, permissionService,
                    nodeInfoOptions.getNodeInfoFlags());

            if (nodeInfo == null) {
                continue;
            }

            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
    }

    private static NodeInfoFlags nodeInfoWithAllFlagsOn = NodeInfoFlags.CreateNewNodeInfoFlagsWithAllFlagsTrue();
    protected NodeInfo nodeRefToNodeInfo(NodeRef nodeRef, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        return nodeRefToNodeInfo(nodeRef, fileFolderService, nodeService,
                                    permissionService, nodeInfoWithAllFlagsOn);
    }

    protected NodeInfo nodeRefToNodeInfo(NodeRef nodeRef,
            IFileFolderService fileFolderService,
            INodeService nodeService,
            IPermissionService permissionService,
            NodeInfoFlags nodeInfoFlags) {
        if (!permissionService.hasPermission(nodeRef, IPermissionService.READ)) {
            logger.warn("Excluding node {} from results due to insufficient permissions", nodeRef);
            return null;
        }

        if (!nodeService.exists(nodeRef)) {
            logger.debug("Excluding node {} from results because it does not exist", nodeRef);
            return null;
        }

        eu.xenit.apix.filefolder.NodePath path =
            createOptionally(nodeRef, fileFolderService::getPath, nodeInfoFlags.getRetrievePath());
        NodeMetadata nodeMetadata =
            createOptionally(nodeRef, nodeService::getMetadata, nodeInfoFlags.getRetrieveMetadata());
        Map<String, PermissionValue> permissions =
            createOptionally(nodeRef, permissionService::getPermissions, nodeInfoFlags.getRetrievePermissions());
        NodeAssociations associations = createNodeAssociationsOptionally(nodeRef, nodeService, nodeInfoFlags);

        return new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
    }

    private static <R> R createOptionally(NodeRef nodeRef, Function<NodeRef, R> transformation, Boolean factCheck) {
        return factCheck ? transformation.apply(nodeRef) : null;
    }

    private static NodeAssociations createNodeAssociationsOptionally(NodeRef nodeRef, INodeService nodeService, NodeInfoFlags nodeInfoFlags) {
        if(!nodeInfoFlags.getRetrieveAssocs()) {
            return null;
        }
        List<ChildParentAssociation> childAssocs =
            createOptionally(nodeRef, nodeService::getChildAssociations, nodeInfoFlags.getRetrieveChildAssocs());
        List<ChildParentAssociation> parentAssociations =
            createOptionally(nodeRef, nodeService::getParentAssociations, nodeInfoFlags.getRetrieveParentAssocs());
        List<NodeAssociation> targetAssociations =
            createOptionally(nodeRef, nodeService::getTargetAssociations, nodeInfoFlags.getRetrieveTargetAssocs());
        List<NodeAssociation> sourceAssociationts =
            createOptionally(nodeRef, nodeService::getSourceAssociations, nodeInfoFlags.getRetrieveSourceAssocs());
        return new NodeAssociations(childAssocs, parentAssociations, targetAssociations, sourceAssociationts);
    }
}
