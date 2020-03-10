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

    protected NodeInfo nodeRefToNodeInfo(NodeRef nodeRef, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        List<NodeRef> nodeRefList = new ArrayList<NodeRef>();
        nodeRefList.add(nodeRef);
        List<NodeInfo> nodeInfoList = this
                .nodeRefToNodeInfo(nodeRefList, fileFolderService, nodeService, permissionService);

        NodeInfo nodeInfo = null;
        if (!nodeInfoList.isEmpty() && nodeInfoList.size() > 0) {
            nodeInfo = nodeInfoList.get(0);
        }

        return nodeInfo;
    }

    protected List<NodeInfo> nodeRefToNodeInfo(List<NodeRef> nodeRefs, IFileFolderService fileFolderService,
            INodeService nodeService, IPermissionService permissionService) {
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        for (NodeRef nodeRef : nodeRefs) {
            eu.xenit.apix.filefolder.NodePath path = fileFolderService.getPath(nodeRef);
            NodeMetadata nodeMetadata = nodeService.getMetadata(nodeRef);
            Map<String, PermissionValue> permissions = permissionService.getPermissions(nodeRef);
            NodeAssociations associations = nodeService.getAssociations(nodeRef);
            NodeInfo nodeInfo = new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
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
            boolean retrieveTargetAssocs) {
        List<NodeInfo> nodeInfoList = nodeRefToNodeInfo(Collections.singletonList(nodeRef), fileFolderService,
                nodeService, permissionService, retrievePath, retrieveMetadata, retrievePermissions, retrieveAssocs,
                retrieveChildAssocs, retrieveParentAssocs, retrieveTargetAssocs);

        return nodeInfoList.get(0);
    }

    protected List<NodeInfo> nodeRefToNodeInfo(List<NodeRef> nodeRefs,
            IFileFolderService fileFolderService,
            INodeService nodeService,
            IPermissionService permissionService,
            boolean retrievePath,
            boolean retrieveMetadata,
            boolean retrievePermissions,
            boolean retrieveAssocs,
            boolean retrieveChildAssocs,
            boolean retrieveParentAssocs,
            boolean retrieveTargetAssocs) {
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        for (NodeRef nodeRef : nodeRefs) {
            if (!permissionService.hasPermission(nodeRef, IPermissionService.READ)) {
                logger.warn("Excluding node {} from results due to insufficient permissions", nodeRef);
                continue;
            }
            eu.xenit.apix.filefolder.NodePath path = null;
            if (retrievePath) {
                path = fileFolderService.getPath(nodeRef);
            }

            logger.debug("start getMetadata");
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
                associations = new NodeAssociations(childAssocs, parentAssociations, targetAssociations);
            }
            NodeInfo nodeInfo = new NodeInfo(nodeRef, nodeMetadata, permissions, associations, path);
            nodeInfoList.add(nodeInfo);
        }

        return nodeInfoList;
    }
}
