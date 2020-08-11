package eu.xenit.apix.rest.v2.nodes;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
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
import eu.xenit.apix.rest.v2.RestV2Config;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV2Config.BaseUrl, families = RestV2Config.Family, defaultFormat = "json",
        description = "Access operations on nodes", value = "Nodes")
@Transaction(
        readOnly = false
)
@Component("eu.xenit.apix.rest.v2.NodesWebscript")
public class NodesWebscriptV2 extends ApixV2Webscript {

    private final static Logger logger = LoggerFactory.getLogger(NodesWebscriptV2.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    IPermissionService permissionService;

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @ApiOperation("Returns combined information of a node")
    @Uri(value = "/nodes/{space}/{store}/{guid}", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    public void getAllInfo(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(nodeRef, this.fileFolderService, this.nodeService, this.permissionService);

        writeJsonResponse(response, nodeInfo);
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
                    "'retrieveMetadata', 'retrievePath', 'retrievePermissions', 'retrieveAssocs', 'retrieveChildAssocs',\n"
                    +
                    "'retrieveParentAssocs', 'retrieveTargetAssocs' are optional parameters.\n" +
                    "Set 'retrieveMetadata' to false to omit the aspects and properties from the result.\n" +
                    "Set 'retrievePath' to false to omit the path from the result.\n" +
                    "Set 'retrievePermissions' to false to omit the permissions from the result.\n" +
                    "Set 'retrieveAssocs' to false to omit the associations (parent associations, child associations, peer associations) from the result.\n"
                    +
                    "Set 'retrieveChildAssocs' to false to omit the child associations from the result.\n" +
                    "Set 'retrieveParentAssocs' to false to omit the parent associations from the result.\n" +
                    "Set 'retrieveTargetAssocs' to false to omit the peer target associations from the result.\n" +
                    "Set 'retrieveSourceAssocs' to false to omit the peer source associations from the result.\n")
    @Uri(value = "/nodes/nodeInfo", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo[].class))
    public void getAllInfos(WebScriptRequest request, WebScriptResponse response) throws IOException, JSONException {
        logger.debug("entered getAllInfo method");
        String requestString = request.getContent().getContent();
        logger.debug("request content: " + requestString);
        JSONObject jsonObject = new JSONObject(requestString);
        if (jsonObject == null) {
            response.setStatus(400);
            String message = String
                    .format("Malfromed body: request string could not be parsed to jsonObject: %s", requestString);
            logger.debug(message);
            writeJsonResponse(response, message);
        }
        logger.debug("json: " + jsonObject.toString());

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
                response.setStatus(400);
                String message = String.format("Could not retrieve target noderefs from body: %s", jsonObject);
                logger.debug(message);
                writeJsonResponse(response, message);
            }
            int nodeRefsJsonArrayLength = nodeRefsJsonArray.length();
            logger.debug("nodeRefsJsonArrayLength: " + nodeRefsJsonArrayLength);
            for (int i = 0; i < nodeRefsJsonArrayLength; i++) {
                String nodeRefString = (String) nodeRefsJsonArray.get(i);
                logger.debug("nodeRefString: " + nodeRefString);
                NodeRef nodeRef = new NodeRef(nodeRefString);
                nodeRefs.add(nodeRef);
            }
        } catch (JSONException e) {
            logger.error("Error deserializing json body", e);
            String message = String.format("Malformed json body {}", jsonObject);
            response.setStatus(400);
            writeJsonResponse(response, message);
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

        logger.debug("start writeJsonResponse");
        writeJsonResponse(response, nodeInfoList);
        logger.debug("end writeJsonResponse");
    }

    @ApiOperation(value = "Retrieve current user's permissions for a node",
            notes = "Returns a key-value map of permissions keys to a value of 'DENY' or 'ALLOW'. " +
                    "Possible keys are: Read, Write, Delete, CreateChildren, ReadPermissions, ChangePermissions, " +
                    "or custom permissions")
    @Uri(value = "/nodes/{space}/{store}/{guid}/permissions", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = PermissionValue.class, responseContainer = "Map"))
    public void getPermissions(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        Map<String, PermissionValue> permissions = this.permissionService.getPermissionsFast(nodeRef);
        writeJsonResponse(response, permissions);
    }

    @ApiOperation("Creates or copies a node")
    @Uri(value = "/nodes", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    public void createNode(final CreateNodeOptions createNodeOptions, WebScriptResponse response) throws IOException {
        Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        NodeRef parent = new NodeRef(createNodeOptions.parent);

                        if (!nodeService.exists(parent)) {
                            response.setStatus(HttpStatus.SC_NOT_FOUND);
                            writeJsonResponse(response, "Parent does not exist");
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
                                response.setStatus(HttpStatus.SC_NOT_FOUND);
                                writeJsonResponse(response, "CopyFrom does not exist");
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
                            response.setStatus(HttpStatus.SC_BAD_REQUEST);
                            writeJsonResponse(response,
                                    "Please provide parameter \"type\" when creating a new node");
                            return null;
                        }
                        metadataChanges = new MetadataChanges(type, null, null,
                                createNodeOptions.properties);
                        try {
                            nodeService.setMetadata(nodeRef, metadataChanges);
                        } catch (RuntimeException ex) {
                            response.setStatus(HttpStatus.SC_BAD_REQUEST);
                            writeJsonResponse(response, ex.getMessage());
                        }
                        return nodeRef;
                    }
                }, false, true);

        NodeRef resultRef = new NodeRef(resultObject.toString());

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

        writeJsonResponse(response, nodeInfo);
    }

    @Deprecated
    @ApiOperation(value = "Creates a new node with given content", consumes = "multipart/form-data")
    @Uri(value = "/nodes/upload", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    @ApiImplicitParams({
            @ApiImplicitParam(value = "Noderef of parent for the new node", name = "parent", paramType = "form", dataType = "string", required = true),
            //TODO: Datatype doesnt work here?
            @ApiImplicitParam(value = "QName type for the new node", name = "type", paramType = "form", dataType = "string", required = false),
            @ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = true)})
    public void uploadNode(final WebScriptRequest multiPart, final WebScriptResponse response) throws IOException {
        logger.error("Deprecated. Please use /v1/nodes/upload instead.");

        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        String type = ContentModel.TYPE_CONTENT.toString();
        String destination = null;
        org.springframework.extensions.webscripts.servlet.FormData.FormField content = null;

        org.springframework.extensions.webscripts.servlet.FormData formData = (org.springframework.extensions.webscripts.servlet.FormData) multiPart
                .parseContent();
        final org.springframework.extensions.webscripts.servlet.FormData.FormField[] fields = formData.getFields();
        for (org.springframework.extensions.webscripts.servlet.FormData.FormField field : fields) {
            if (field.getName().equals("parent")) {
                destination = field.getValue();
            } else if (field.getName().equals("file") && field.getIsFile()) {
                content = field;
            } else if (field.getName().equals("type")) {
                type = field.getValue();
            }
        }

        if (content == null) {
            throw new IllegalArgumentException("Content must be supplied as a multipart 'file' field");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Must supply a 'parent' field");
        }

        final String finalDestination = destination;
        final org.springframework.extensions.webscripts.servlet.FormData.FormField finalContent = content;
        final String finalType = type;
        Object resultRef = transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        NodeRef newNode = nodeService
                                .createNode(new NodeRef(finalDestination), finalContent.getFilename(),
                                        new QName(finalType));

                        nodeService.setContent(newNode, finalContent.getInputStream(), finalContent.getFilename());
                        return newNode;
                    }
                }, false, true);

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo((NodeRef) resultRef, fileFolderService, nodeService, permissionService);

        writeJsonResponse(response, nodeInfo);
    }
}
