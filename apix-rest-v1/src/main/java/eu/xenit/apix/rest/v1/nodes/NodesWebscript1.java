package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.ContentInputStream;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.filefolder.NodePath;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.node.NodeAssociation;
import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.NodePermission;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import eu.xenit.apix.rest.v1.nodes.ChangeAclsOptions.Access;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.stereotype.Component;


@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Access operations on nodes", value = "Nodes")
@Transaction(readOnly = false)
@Component("eu.xenit.apix.rest.v1.NodesWebscript")
public class NodesWebscript1 extends ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(NodesWebscript1.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    IPermissionService permissionService;

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Uri(value = "/nodes/{space}/{store}/{guid}/metadata", method = HttpMethod.POST)
    @ApiOperation(value = "Change node metadata",
            notes = "Example to set a node's title and add the sys:versionable aspect:\n" +
                    "\n" +
                    "```\n" +
                    "POST /apix/v1/nodes/workspace/SpacesStore/b54287de-381e-44b1-b6d1-e6c9a9d632fd/metadata\n" +
                    "{\n" +
                    "  \"aspectsToAdd\": [\"{http://www.alfresco.org/model/system/1.0}temporary\"],\n" +
                    "  \"propertiesToSet\": {\"{http://www.alfresco.org/model/content/1.0}title\":[\"My new title\"]}\n"
                    +
                    "}\n" +
                    "```\n" +
                    "\n" +
                    "When you generalize the type of a node instead of specializing, the default properties of the " +
                    "initial **type** that are not present in the new type are removed, however, the default aspects " +
                    "are not. This is the default behaviour. If you also want to remove these aspects on type " +
                    "generalization, add the parameter **cleanUpAspectsOnGeneralization** to the request body.\n" +
                    "\n" +
                    "Example for cleaning up aspects on type generalization:\n" +
                    "\n" +
                    "```\n" +
                    "POST /apix/v1/nodes/workspace/SpacesStore/b54287de-381e-44b1-b6d1-e6c9a9d632fd/metadata\n" +
                    "{\n" +
                    "  \"type\": \"{http://www.alfresco.org/model/content/1.0}content\",\n" +
                    "  \"cleanUpAspectsOnGeneralization\": true\n" +
                    "}\n" +
                    "```\n")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeMetadata.class))
    public void setMetadata(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid,
            final MetadataChanges changes, final WebScriptResponse response) throws IOException {
        NodeRef nodeRef = createNodeRef(space, store, guid);
        NodeMetadata nodeMetadata = nodeService.setMetadata(nodeRef, changes);
        if (nodeMetadata == null) {
            response.setStatus(404);
        }
        writeJsonResponse(response, nodeMetadata);
    }

    @ApiOperation("Retrieve node metadata")
    @Uri(value = "/nodes/{space}/{store}/{guid}/metadata", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeMetadata.class))
    public void getMetadata(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {

        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodeMetadata nodeMetadata = nodeService.getMetadata(nodeRef);
        if (nodeMetadata == null) {
            response.setStatus(404);
        } else {
            writeJsonResponse(response, nodeMetadata);
        }
    }


    @ApiOperation("Delete a node")
    @Uri(value = "/nodes/{space}/{store}/{guid}", method = HttpMethod.DELETE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")})
    public void deleteNode(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid,
            @RequestParam(required = false) final String permanently, final WebScriptResponse response)
            throws IOException {
        logger.debug(" permanently: " + permanently);
        boolean deletePermanently = permanently != null && permanently.equals("true");
        logger.debug(" deletePermanently: " + deletePermanently);
        NodeRef nodeRef = createNodeRef(space, store, guid);
        try {
            boolean success = nodeService.deleteNode(nodeRef, deletePermanently);
            if (success) {
                logger.debug("node {} deleted", nodeRef);
                response.setStatus(200);
                writeJsonResponse(response, "Node deleted.");
            } else {
                logger.debug("Failed to delete node, node does not exist: {}", nodeRef);
                response.setStatus(404);
                writeJsonResponse(response, "Failed to delete node, node does not exist: " + nodeRef.toString());
            }
        } catch (AccessDeniedException accessDeniedException) {
            logger.debug("Not authorized to delete node: {}", nodeRef, accessDeniedException);
            response.setStatus(403);
            writeJsonResponse(response, "Not authorized to delete node");
        }
    }

    @ApiOperation("Retrieve node associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeAssociations.class))
    public void getAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodeAssociations associations = this.nodeService.getAssociations(nodeRef);

        writeJsonResponse(response, associations);
    }

    @ApiOperation("Create new association with given node as source")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void createAssociation(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            CreateAssociationOptions options,
            WebScriptResponse response) throws IOException {
        NodeRef source = this.createNodeRef(space, store, guid);
        nodeService.createAssociation(source, options.getTarget(), options.getType());

        //writeJsonResponse(response, associations);
        response.setStatus(200);
    }

    @ApiOperation("Deletes an association with given node as source")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void deleteAssociation(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @RequestParam String target, @RequestParam String type,
            WebScriptResponse response) throws IOException {
        NodeRef source = this.createNodeRef(space, store, guid);
        nodeService.removeAssociation(source, new NodeRef(target), new QName(type));
        response.setStatus(200);
    }


    @ApiOperation("Retrieve node parent associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/parents", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class))
    public void getParentAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        List<ChildParentAssociation> associations = this.nodeService.getParentAssociations(nodeRef);

        writeJsonResponse(response, associations);
    }

    @ApiOperation("Retrieve node child associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/children", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class))
    public void getChildAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        List<ChildParentAssociation> associations = this.nodeService.getChildAssociations(nodeRef);

        writeJsonResponse(response, associations);
    }

    @ApiOperation("Retrieve node peer associations with given node being the source")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/targets", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeAssociation[].class))
    public void getSourcePeerAssociations(@UriVariable String space, @UriVariable String store,
            @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        List<NodeAssociation> associations = this.nodeService.getTargetAssociations(nodeRef);

        writeJsonResponse(response, associations);
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

        Map<String, PermissionValue> permissions = this.permissionService.getPermissions(nodeRef);
        writeJsonResponse(response, permissions);
    }

    @ApiOperation(value = "sets a user a permission for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void setPermission(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @UriVariable String authority, @UriVariable String permission, WebScriptResponse response)
            throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        this.permissionService.setPermission(nodeRef, authority, permission);
        response.setStatus(200);
    }

    @ApiOperation(value = "removes a user its permission for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void deletePermission(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @UriVariable String authority, @UriVariable String permission, WebScriptResponse response)
            throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        this.permissionService.deletePermission(nodeRef, authority, permission);
        response.setStatus(200);
    }

    @ApiOperation(value = "Gets the ACLs for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/acl", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodePermission.class))
    public void getAcls(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodePermission permissions = this.permissionService.getNodePermissions(nodeRef);
        response.setStatus(200);
        writeJsonResponse(response, permissions);
    }

    @Uri(value = "/nodes/{space}/{store}/{guid}/acl/inheritFromParent", method = HttpMethod.POST)
    public void setInheritParentPermissions(@UriVariable String space, @UriVariable String store,
            @UriVariable String guid, final InheritFromParent inherit) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        this.permissionService.setInheritParentPermissions(nodeRef, inherit.inheritFromParent);
    }

    @ApiOperation(value = "Sets the ACL for a node.", notes = "Example:\n" +
            "\n" +
            "```\n" +
            "{\n" +
            "   \"inheritFromParent\": false,\n" +
            "   \"ownAccessList\": [\n" +
            "      {\n" +
            "         \"allowed\": true,\n" +
            "         \"authority\": \"MYGROUP\",\n" +
            "         \"permission\": \"Collaborator\"\n" +
            "      }\n" +
            "]}\n" +
            "```", consumes = "application/json")
    @Uri(value = "/nodes/{space}/{store}/{guid}/acl", method = HttpMethod.PUT)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void setAcls(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final ChangeAclsOptions changeAclsOptions, WebScriptRequest request, WebScriptResponse response)
            throws JSONException, IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodePermission permissions = new NodePermission();

        permissions.setInheritFromParent(changeAclsOptions.isInheritFromParent());
        Set<NodePermission.Access> accessList = new HashSet<>();
        permissions.setOwnAccessList(accessList);
        for (Access access : changeAclsOptions.getOwnAccessList()) {
            NodePermission.Access a = new NodePermission.Access();
            accessList.add(a);
            a.setAllowed(access.allowed);
            a.setAuthority(access.authority);
            a.setPermission(access.permission);
        }
        this.permissionService.setNodePermissions(nodeRef, permissions);

        response.setStatus(200);
    }

    @ApiOperation("Returns path of the node")
    @Uri(value = "/nodes/{space}/{store}/{guid}/path", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodePath.class))
    public void getPath(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodePath path = this.fileFolderService.getPath(nodeRef);
        writeJsonResponse(response, path);
    }

    @ApiOperation("Returns combined information of a node")
    @Uri(value = "/nodes/{space}/{store}/{guid}", method = HttpMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void getAllInfoOfNode(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        try {
            NodeInfo nodeInfo = this
                    .nodeRefToNodeInfo(nodeRef, this.fileFolderService, this.nodeService, this.permissionService);
            writeJsonResponse(response, nodeInfo);
        } catch (InvalidNodeRefException invalidNodeRefException) {
            logger.debug("Failed to get node info, node does not exist: {}", invalidNodeRefException);
            response.setStatus(404);
            writeJsonResponse(response, "Failed to get node info, node does not exist.");
        } catch (AccessDeniedException accessDeniedException) {
            logger.debug("Failed to get node info, not authorized for node: {}", accessDeniedException);
            response.setStatus(403);
            writeJsonResponse(response, "Failed to get node info, not authorized for node");
        }
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
                    "Set 'retrieveAssocs' to false to omit the associations(parent associations, child associations, peer associations) from the result.\n"
                    +
                    "Set 'retrieveChildAssocs' to false to omit the child associations from the result.\n" +
                    "Set 'retrieveParentAssocs' to false to omit the parent associations from the result.\n" +
                    "Set 'retrieveTargetAssocs' to false to omit the peer associations from the result.\n")
    @Uri(value = "/nodes/nodeInfo", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo[].class))
    public void getAllInfoOfNodes(WebScriptRequest request, WebScriptResponse response)
            throws IOException, JSONException {
        String requestString = request.getContent().getContent();
        logger.debug("request content: " + requestString);
        JSONObject jsonObject = new JSONObject(requestString);
        if (jsonObject == null) {
            throw new NullPointerException("jsonObject is null");
        }
        logger.debug("json: " + jsonObject.toString());

        boolean retrieveMetadata = true;
        boolean retrievePath = true;
        boolean retrievePermissions = true;
        boolean retrieveAssocs = true;
        boolean retrieveChildAssocs = true;
        boolean retrieveParentAssocs = true;
        boolean retrieveTargetAssocs = true;

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

            JSONArray nodeRefsJsonArray = jsonObject.getJSONArray("noderefs");
            if (nodeRefsJsonArray == null) {
                logger.error("noderefsJsonArray is null");
                throw new NullPointerException("noderefsJsonArray is null");
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
            e.printStackTrace();
            return;
        }

        //logger.error("done parsing request data");
        //logger.error("start nodeRefToNodeInfo");
        List<NodeInfo> nodeInfoList = this.nodeRefToNodeInfo(nodeRefs,
                this.fileFolderService,
                this.nodeService,
                this.permissionService,
                retrievePath,
                retrieveMetadata,
                retrievePermissions,
                retrieveAssocs,
                retrieveChildAssocs,
                retrieveParentAssocs,
                retrieveTargetAssocs);
        //logger.error("end nodeRefToNodeInfo");

        //logger.error("start writeJsonResponse");
        writeJsonResponse(response, nodeInfoList);
        //logger.error("end writeJsonResponse");
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
                        NodeRef copyFrom = null;

                        if (createNodeOptions.copyFrom != null) {
                            copyFrom = new NodeRef(createNodeOptions.copyFrom);
                        }

                        NodeRef nodeRef;
                        if (copyFrom == null) {
                            nodeRef = nodeService
                                    .createNode(parent, createNodeOptions.properties, new QName(createNodeOptions.type),
                                            null);
                        } else {
                            nodeRef = nodeService.copyNode(copyFrom, parent, true);
                        }
                        return nodeRef;
                    }
                }, false, true);

        NodeRef resultRef = new NodeRef(resultObject.toString());

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

        writeJsonResponse(response, nodeInfo);
    }

    @ApiOperation("Moves a node by changing its parent")
    @Uri(value = "/nodes/{space}/{store}/{guid}/parent", method = HttpMethod.PUT)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void setParent(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, final ChangeParentOptions location, WebScriptResponse response)
            throws IOException {
        NodeRef parent = new NodeRef(location.parent);
        NodeRef nodeToMove = createNodeRef(space, store, guid);
        nodeService.moveNode(nodeToMove, parent);
    }

    @ApiOperation(value = "Downloads content file for given node")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void getContent(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        final NodeRef nodeRef = new NodeRef(space, store, guid);
        ContentInputStream contentInputStream = nodeService.getContent(nodeRef);
        if (contentInputStream == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType(contentInputStream.getMimetype());
        response.addHeader("Content-Length", Objects.toString(contentInputStream.getSize()));

        // Unwrap the webscript responses, because we don't want any caching when writing the file
        WebScriptResponse resp = response;
        while (resp instanceof WrappingWebScriptResponse) {
            resp = ((WrappingWebScriptResponse) resp).getNext();
        }

        InputStream inputStream = contentInputStream.getInputStream();

        try {
            IOUtils.copyLarge(inputStream, resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @ApiOperation(value = "Sets or updates the content for given node. If no file is given the content will be set to empty.", consumes = "multipart/form-data")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content", method = HttpMethod.PUT)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiImplicitParams({@ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = false)})
    public void setContent(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final WebScriptRequest multiPart, WebScriptResponse response) throws IOException {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        org.springframework.extensions.webscripts.servlet.FormData.FormField content = null;

        org.springframework.extensions.webscripts.servlet.FormData formData = (org.springframework.extensions.webscripts.servlet.FormData) multiPart
                .parseContent();
        final org.springframework.extensions.webscripts.servlet.FormData.FormField[] fields = formData.getFields();
        for (org.springframework.extensions.webscripts.servlet.FormData.FormField field : fields) {
            if (field.getName().equals("file") && field.getIsFile()) {
                content = field;
            }
        }

        final NodeRef finalDestination = this.createNodeRef(space, store, guid);
        final org.springframework.extensions.webscripts.servlet.FormData.FormField finalContent = content;
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {

                nodeService.setContent(finalDestination, finalContent != null ? finalContent.getInputStream() : null,
                        finalContent != null ? finalContent.getFilename() : null);
                return null;
            }
        }, false, true);

        //writeJsonResponse(response, new Message("Successfully set content"));
    }


    @ApiOperation(value = "Sets the content for given node to empty")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void deleteContent(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final WebScriptRequest multiPart, WebScriptResponse response) throws IOException {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        final NodeRef finalDestination = this.createNodeRef(space, store, guid);
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {

                nodeService.setContent(finalDestination, null, null);
                return null;
            }
        }, false, true);
    }


    @ApiOperation(value = "Checks if the given node exists")
    @Uri(value = "/nodes/{space}/{store}/{guid}/exists", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void exists(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        writeJsonResponse(response, nodeService.exists(new NodeRef(space, store, guid)));
    }


    @ApiOperation(value = "Creates a new node with given content", consumes = "multipart/form-data")
    @Uri(value = "/nodes/upload", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
    @ApiImplicitParams({
            @ApiImplicitParam(value = "Noderef of parent for the new node", name = "parent", paramType = "form", dataType = "string", required = true),
            //TODO: Datatype doesnt work here?
            @ApiImplicitParam(value = "QName type for the new node", name = "type", paramType = "form", dataType = "string", required = false),
            @ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = true),
            @ApiImplicitParam(value = "Metadata for this file", name = "metadata", paramType = "form", dataType = "string", required = false),
            @ApiImplicitParam(value = "Enable metadata extraction from the content, for example for msg files", name = "extractMetadata", paramType = "form", dataType = "boolean", required = false),
    })

    public void uploadNode(final WebScriptRequest multiPart, final WebScriptResponse response) throws IOException {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        // Note the difference between:
        //  * metadata        = The metadata the user annotates the file with.
        //  * extractMetadata = Whether the users wants metadata automatically extracted from the file.
        // Both setting metadata and extracting metadata are optional. They can happen (or not) independently from each other.
        String type = ContentModel.TYPE_CONTENT.toString();
        String parent = null;
        FormData.FormField file = null;
        Boolean extractMetadata = false;
        MetadataChanges metadata = null;

        FormData formData = (FormData) multiPart.parseContent();
        for (FormData.FormField field : formData.getFields()) {
            if (field.getName().equals("parent")) {
                parent = field.getValue();
            } else if (field.getName().equals("type")) {
                type = field.getValue();
            } else if (field.getName().equals("file") && field.getIsFile()) {
                file = field;
            } else if (field.getName().equals("metadata")) {
                ObjectMapper mapper = new ObjectMapper();
                metadata = mapper.readValue(field.getValue(), MetadataChanges.class);
            } else if (field.getName().equals("extractMetadata")) {
                extractMetadata = Boolean.parseBoolean(field.getValue());
            }
        }

        if (file == null) {
            throw new IllegalArgumentException("Content must be supplied as a multipart 'file' field");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Must supply a 'parent' field");
        }

        final String finalParent = parent;
        final FormData.FormField finalFile = file;
        final String finalType = type;
        final MetadataChanges finalMetadata = metadata;
        final Boolean finalExtractMetadata = extractMetadata;
        Object resultRef = transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        NodeRef newNode = nodeService
                                .createNode(new NodeRef(finalParent), finalFile.getFilename(), new QName(finalType));
                        nodeService.setContent(newNode, finalFile.getInputStream(), finalFile.getFilename());

                        if (finalMetadata != null) {
                            nodeService.setMetadata(newNode, finalMetadata);
                        }

                        if (finalExtractMetadata) {
                            try {
                                nodeService.extractMetadata(newNode);
                                logger.debug("Metadata extracted");
                            } catch (Exception ex) {
                                logger.warn("Exception while extracting metadata", ex);
                            }
                        }
                        return newNode;
                    }
                }, false, true);

        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo((NodeRef) resultRef, fileFolderService, nodeService, permissionService);
        writeJsonResponse(response, nodeInfo);
    }
}
