package eu.xenit.apix.rest.v2.nodes;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.nodes.CreateNodeOptions;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.rest.v2.ApixV2Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.alfresco.service.ServiceRegistry;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication
@RestController("eu.xenit.apix.rest.v2.NodesWebscript")
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

    @ApiOperation("Returns combined information of a node." +
            "\nNote: versionstore does not support sourceAssocs. " +
            "For version nodes, an empty list added to the result")
    @GetMapping(value = "/v2/nodes/{space}/{store}/{guid}")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    public ResponseEntity<NodeInfo> getAllInfo(@PathVariable String space,
                                               @PathVariable String store,
                                               @PathVariable String guid) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(nodeRef, this.fileFolderService, this.nodeService, this.permissionService);

        return writeJsonResponse(nodeInfo);
    }

    @ApiOperation(value = "Returns combined information of multiple nodes",
            notes = "Example to get combined information of multiple nodes:\n" +
                    "\n" +
                    "```\n" +
                    "POST /apix/v1/nodes/nodeInfo\n" +
                    "{\n" +
                    "\"retrieveMetadata\" : true, \n" +
                    "\"retrievePath\" : true, \n" +
                    "\"retrievePermissions\" : true, \n" +
                    "\"retrieveAssocs\" : true, \n" +
                    "\"retrieveChildAssocs\" : true, \n" +
                    "\"retrieveParentAssocs\" : true, \n" +
                    "\"retrieveTargetAssocs\" : true, \n" +
                    "\"noderefs\": [ \n" +
                    "  \"workspace://SpacesStore/123456789\", \n" +
                    "  \"workspace://SpacesStore/147258369\", \n" +
                    "  \"workspace://SpacesStore/478159236\" \n" +
                    "]}\n" +
                    "```" +
                    "\n" +
                    "'retrieveMetadata', 'retrievePath', 'retrievePermissions', 'retrieveAssocs', " +
                    "'retrieveChildAssocs',\n"
                    +
                    "'retrieveParentAssocs', 'retrieveTargetAssocs' are optional parameters.\n" +
                    "Set 'retrieveMetadata' to false to omit the aspects and properties from the result.\n" +
                    "Set 'retrievePath' to false to omit the path from the result.\n" +
                    "Set 'retrievePermissions' to false to omit the permissions from the result.\n" +
                    "Set 'retrieveAssocs' to false to omit the associations (parent associations, child associations," +
                    " peer associations) from the result.\n"
                    +
                    "Set 'retrieveChildAssocs' to false to omit the child associations from the result.\n" +
                    "Set 'retrieveParentAssocs' to false to omit the parent associations from the result.\n" +
                    "Set 'retrieveTargetAssocs' to false to omit the peer target associations from the result.\n" +
                    "Set 'retrieveSourceAssocs' to false to omit the peer source associations from the result. " +
                    "Note: versionstore does not support sourceAssocs. For version nodes, an empty list added to the " +
                    "result\n")
    @PostMapping(value = "/v2/nodes/nodeInfo")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo[].class))
    // TODO FIXME @Zlatin Alfresco MVC POJO MUCH? WTF.
    public ResponseEntity<?> getAllInfos(@RequestBody final String requestString) throws JSONException {
        logger.debug("entered getAllInfo method");
        if (requestString == null || requestString.isEmpty()) {
            String message = String
                    .format("Malfromed body: request string could not be parsed to jsonObject: %s", requestString);
            logger.debug(message);
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(message);
        }
        logger.debug("request content: {}", requestString);
        JSONObject jsonObject = new JSONObject(requestString);
        logger.debug("json: {}", jsonObject);

        boolean retrieveMetadata = true;
        boolean retrievePath = true;
        boolean retrievePermissions = true;
        boolean retrieveAssocs = true;
        boolean retrieveChildAssocs = true;
        boolean retrieveParentAssocs = true;
        boolean retrieveTargetAssocs = true;
        boolean retrieveSourceAssocs = true;

        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        try {
            if (jsonObject.has("retrieveMetadata")) {
                retrieveMetadata = jsonObject.getBoolean("retrieveMetadata");
            }
            if (jsonObject.has("retrievePath")) {
                retrievePath = jsonObject.getBoolean("retrievePath");
            }
            if (jsonObject.has("retrievePermissions")) {
                retrievePermissions = jsonObject.getBoolean("retrievePermissions");
            }
            if (jsonObject.has("retrieveAssocs")) {
                retrieveAssocs = jsonObject.getBoolean("retrieveAssocs");
            }
            if (jsonObject.has("retrieveChildAssocs")) {
                retrieveChildAssocs = jsonObject.getBoolean("retrieveChildAssocs");
            }
            if (jsonObject.has("retrieveParentAssocs")) {
                retrieveParentAssocs = jsonObject.getBoolean("retrieveParentAssocs");
            }
            if (jsonObject.has("retrieveTargetAssocs")) {
                retrieveTargetAssocs = jsonObject.getBoolean("retrieveTargetAssocs");
            }
            if (jsonObject.has("retrieveSourceAssocs")) {
                retrieveSourceAssocs = jsonObject.getBoolean("retrieveSourceAssocs");
            }

            JSONArray nodeRefsJsonArray = jsonObject.getJSONArray("noderefs");
            if (nodeRefsJsonArray == null) {
                String message = String.format("Could not retrieve target noderefs from body: %s", jsonObject);
                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .body(message);
            }
            int nodeRefsJsonArrayLength = nodeRefsJsonArray.length();
            logger.debug("nodeRefsJsonArrayLength: {}", nodeRefsJsonArrayLength);
            for (int i = 0; i < nodeRefsJsonArrayLength; i++) {
                String nodeRefString = (String) nodeRefsJsonArray.get(i);
                logger.debug("nodeRefString: {}", nodeRefString);
                NodeRef nodeRef = new NodeRef(nodeRefString);
                nodeRefs.add(nodeRef);
            }
        } catch (JSONException e) {
            logger.error("Error deserializing json body", e);
            String message = String.format("Malformed json body %s", jsonObject);
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(message);
        }

        logger.debug("done parsing request data");
        logger.debug("start nodeRefToNodeInfo");
        List<NodeInfo> nodeInfoList = this.nodeRefToNodeInfo(
                nodeRefs,
                this.fileFolderService,
                this.nodeService,
                this.permissionService,
                retrievePath,
                retrieveMetadata,
                retrievePermissions,
                retrieveAssocs,
                retrieveChildAssocs,
                retrieveParentAssocs,
                retrieveTargetAssocs,
                retrieveSourceAssocs);
        logger.debug("end nodeRefToNodeInfo");

        logger.debug("writeJsonResponse");
        return writeJsonResponse(nodeInfoList);
    }

    @ApiOperation(value = "Retrieve current user's permissions for a node",
            notes = "Returns a key-value map of permissions keys to a value of 'DENY' or 'ALLOW'. " +
                    "Possible keys are: Read, Write, Delete, CreateChildren, ReadPermissions, ChangePermissions, " +
                    "or custom permissions")
    @GetMapping(value = "/v2/nodes/{space}/{store}/{guid}/permissions")
    @ApiResponses(
            @ApiResponse(code = 200, message = "Success", response = PermissionValue.class, responseContainer = "Map"))
    public ResponseEntity<Map<String, PermissionValue>> getPermissions(@PathVariable String space,
                                                                       @PathVariable String store,
                                                                       @PathVariable String guid) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        return writeJsonResponse(
            this.permissionService.getPermissionsFast(nodeRef)
        );
    }

    @ApiOperation("Creates or copies a node")
    @PostMapping(value = "/v2/nodes")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    public ResponseEntity<?> createNode(@RequestBody final CreateNodeOptions createNodeOptions) {
        final StringBuilder errorMessage = new StringBuilder();
        final AtomicInteger errorCode = new AtomicInteger();
        Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    NodeRef parent = new NodeRef(createNodeOptions.parent);

                    if (!nodeService.exists(parent)) {
                        errorCode.addAndGet(HttpStatus.SC_NOT_FOUND);
                        errorMessage.append("Parent does not exist");
                        return null;
                    }

                    NodeRef nodeRef;
                    NodeRef copyFrom = null;
                    if (createNodeOptions.copyFrom == null) {
                        nodeRef = nodeService
                                .createNode(parent, createNodeOptions.name,
                                        new QName(createNodeOptions.type));
                    } else {
                        copyFrom = new NodeRef(createNodeOptions.copyFrom);
                        if (!nodeService.exists(copyFrom)) {
                            errorCode.addAndGet(HttpStatus.SC_NOT_FOUND);
                            errorMessage.append("CopyFrom does not exist");
                            return null;
                        }
                        nodeRef = nodeService.copyNode(copyFrom, parent, true);
                    }

                    MetadataChanges metadataChanges;
                    QName type;
                    if (createNodeOptions.type != null) {
                        type = new QName(createNodeOptions.type);
                    } else if ( createNodeOptions.type == null && createNodeOptions.copyFrom != null ) {
                        type = nodeService.getMetadata(copyFrom).type;
                    } else {
                        errorCode.addAndGet(HttpStatus.SC_BAD_REQUEST);
                        errorMessage.append(
                            "Please provide parameter \"type\" when creating a new node"
                        );
                        return null;
                    }
                    metadataChanges = new MetadataChanges(type, null, null,
                            createNodeOptions.properties);
                    nodeService.setMetadata(nodeRef, metadataChanges);

                    return nodeRef;
                }, false, true);

        if(resultObject == null) {
            return ResponseEntity.status(errorCode.get())
                    .body(errorMessage.toString());
        }

        NodeRef resultRef = new NodeRef(resultObject.toString());

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

        return writeJsonResponse(nodeInfo);
    }
}
