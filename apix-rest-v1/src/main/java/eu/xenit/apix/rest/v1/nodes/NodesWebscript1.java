package eu.xenit.apix.rest.v1.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.ExceptionHandler;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.comments.Conversation;
import eu.xenit.apix.comments.ICommentService;
import eu.xenit.apix.data.ContentInputStream;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.exceptions.FileExistsException;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
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
    ICommentService commentService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    Repository repository;

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
                    "```\n" +
                    "\n" +
                    "Changing the cm:name property will also update the qname path of the node so it is in sync with it.\n"
                    +
                    "This only applies to nodes of type or subtype cm:content or cm:folder but not of type or subtype of cm:systemfolder.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeMetadata.class),
            @ApiResponse(code = 403, message = "Not Authorized")
    })
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeMetadata.class),
            @ApiResponse(code = 403, message = "Not Authorized")
    })
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

    }

    @ApiOperation("Retrieve node associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeAssociations.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void getAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodeAssociations associations = this.nodeService.getAssociations(nodeRef);
        writeJsonResponse(response, associations);

    }

    @ApiOperation("Create new association with given node as source")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations", method = HttpMethod.POST)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized")})
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void deleteAssociation(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @RequestParam String target, @RequestParam String type,
            WebScriptResponse response) throws IOException {
        NodeRef source = this.createNodeRef(space, store, guid);
        NodeRef targetNode = new NodeRef(URLDecoder.decode(target, StandardCharsets.UTF_8.toString()));

        nodeService.removeAssociation(source,
                targetNode,
                new QName(type));
        response.setStatus(200);

    }


    @ApiOperation("Retrieve node parent associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/parents", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void getParentAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        List<ChildParentAssociation> associations = this.nodeService.getParentAssociations(nodeRef);

        writeJsonResponse(response, associations);

    }

    @ApiOperation("Retrieve node child associations")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/children", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void getChildAssociations(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        List<ChildParentAssociation> associations = this.nodeService.getChildAssociations(nodeRef);

        writeJsonResponse(response, associations);

    }

    @ApiOperation("Retrieve node peer associations with given node being the source")
    @Uri(value = "/nodes/{space}/{store}/{guid}/associations/targets", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = PermissionValue.class, responseContainer = "Map")})
    // It would seem permissions can always be retrieved?
    public void getPermissions(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        Map<String, PermissionValue> permissions = this.permissionService.getPermissions(nodeRef);
        writeJsonResponse(response, permissions);

    }

    @ApiOperation(value = "sets a user a permission for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}", method = HttpMethod.POST)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void setPermission(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @UriVariable String authority, @UriVariable String permission, WebScriptResponse response)
            throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        this.permissionService.setPermission(nodeRef, authority, permission);
        response.setStatus(200);

    }

    @ApiOperation(value = "removes a user its permission for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}", method = HttpMethod.DELETE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void deletePermission(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @UriVariable String authority, @UriVariable String permission, WebScriptResponse response)
            throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        this.permissionService.deletePermission(nodeRef, authority, permission);
        response.setStatus(200);

    }

    @ApiOperation(value = "Gets the ACLs for a node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/acl", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodePermission.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void getAcls(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodePermission permissions = this.permissionService.getNodePermissions(nodeRef);
        response.setStatus(200);
        writeJsonResponse(response, permissions);

    }

    @Uri(value = "/nodes/{space}/{store}/{guid}/acl/inheritFromParent", method = HttpMethod.POST)
    @ApiResponses({@ApiResponse(code = 403, message = "Not Authorized")})
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void setAcls(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final ChangeAclsOptions changeAclsOptions, WebScriptRequest request, WebScriptResponse response)
            throws JSONException, IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodePermission permissions = new NodePermission();

        permissions.setInheritFromParent(changeAclsOptions.isInheritFromParent());
        Set<NodePermission.Access> accessList = new HashSet<>();
        permissions.setOwnAccessList(accessList);
        Set<ChangeAclsOptions.Access> ownAccessList = changeAclsOptions.getOwnAccessList();
        if (ownAccessList == null) {
            response.setStatus(400);
            return;
        }
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodePath.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
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
            // This method will return a null result if user has insufficient permissions
            NodeInfo nodeInfo = this
                    .nodeRefToNodeInfo(nodeRef, this.fileFolderService, this.nodeService, this.permissionService);
            if (nodeInfo != null) {
                writeJsonResponse(response, nodeInfo);
            } else {
                String message = String.format("Insufficient permissions for node %s", nodeRef);
                throw new AccessDeniedException(message);
            }
        } catch (InvalidNodeRefException invalidNodeRefException) {
            logger.debug("Failed to get node info, node does not exist: {}", invalidNodeRefException);
            response.setStatus(404);
            writeJsonResponse(response, "Failed to get node info, node does not exist.");
        }

    }

    @ApiOperation(value = "Returns combined information of multiple nodes. "
            + "Nodes errors or without appropriate permissions will not be included in return, "
            + "and will not cause failure of the call.",
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
                    "Set 'retrieveTargetAssocs' to false to omit the peer target associations from the result.\n" +
                    "Set 'retrieveSourceAssocs' to false to omit the peer source associations from the result.\n")
    @Uri(value = "/nodes/nodeInfo", method = HttpMethod.POST)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeInfo[].class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    public void getAllInfoOfNodes(WebScriptRequest request, WebScriptResponse response)
            throws IOException, JSONException {
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
                retrieveTargetAssocs,
                retrieveSourceAssocs);
        //logger.error("end nodeRefToNodeInfo");

        //logger.error("start writeJsonResponse");
        writeJsonResponse(response, nodeInfoList);
        //logger.error("end writeJsonResponse");
    }

    @ApiOperation(value = "Retrieves the ancestors of the nodes",
            notes = "It is possible to add \"root\" as a request parameter.\n"
                    + "It is the node reference up to which point ancestors will be retrieved.\n"
                    + "The default root reference will be the reference of Company Home")
    @Uri(value = "/nodes/{space}/{store}/{guid}/ancestors", method = HttpMethod.GET)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = AncestorsObject.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void retrieveAncestors(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @RequestParam(required = false) String root, WebScriptResponse response) throws IOException {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodeRef rootRef = null;
        if (root != null) {
            rootRef = new NodeRef(root);
        }

        try {
            List<NodeRef> ancestors = nodeService.getAncestors(nodeRef, rootRef);
            AncestorsObject ancestorsObject = new AncestorsObject(nodeRef, ancestors);
            writeJsonResponse(response, ancestorsObject);
        } catch (InvalidNodeRefException ex) {
            logger.error("noderef does not exist");
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            writeJsonResponse(response, ex.getMessage());
        } catch (AccessDeniedException ex) {
            logger.error("access denied on noderef");
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            writeJsonResponse(response, ex.getMessage());
        }
    }

    @ApiOperation(value = "Creates or copies a node",
        notes = "Example of POST body:\n"
                + "\n"
                + "```\n"
                + "POST /apix/v1/nodes\n"
                + "{\n"
                + "\"parent\" : \"workspace://SpacesStore/d5dac928-e581-4507-9be7-9a2416adc318\", \n"
                + "\"name\" : \"mydocument.txt\", \n"
                + "\"type\" : \"{http://www.alfresco.org/model/content/1.0}content\", \n"
                + "\"properties\" : {\n"
                + "      \"{namespace}property1\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"{namespace}property2\": [\n"
                + "        \"string\"\n"
                + "      ],\n"
                + "      \"{namespace}property3\": [\n"
                + "        \"string\"\n"
                + "      ]\n"
                + "}, \n"
                + "\"aspectsToAdd\" : [\n"
                + "      \"{namespace}aspect1\"\n"
                + "], \n"
                + "\"aspectsToRemove\" : [\n"
                + "      \"{namespace}aspect1\"\n"
                + "], \n"
                + "\"copyFrom\" : \"workspace://SpacesStore/f0d15919-3841-4170-807f-b81d2ebdeb80\", \n"
                + "}\n"
                + "```"
                + "\n"
                + "\"aspectsToRemove\" is only relevant when copying a node.\n")
    @Uri(value = "/nodes", method = HttpMethod.POST)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public void createNode(final CreateNodeOptions createNodeOptions, WebScriptResponse response) throws IOException {
        try {
            Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                        @Override
                        public Object execute() throws Throwable {
                            NodeRef parent = new NodeRef(createNodeOptions.parent);

                            if (!nodeService.exists(parent)) {
                                writeNotFoundResponse(response, parent);
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
                            } else if (createNodeOptions.type == null && createNodeOptions.copyFrom != null) {
                                type = nodeService.getMetadata(copyFrom).type;
                            } else {
                                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                                writeJsonResponse(response,
                                        "Please provide parameter \"type\" when creating a new node");
                                return null;
                            }

                            metadataChanges = new MetadataChanges(type,
                                    createNodeOptions.aspectsToAdd,
                                    createNodeOptions.aspectsToRemove,
                                    createNodeOptions.properties);
                            nodeService.setMetadata(nodeRef, metadataChanges);

                            return nodeRef;
                        }
                    }, false, true);

            if (resultObject == null) {
                return;
            }
            NodeRef resultRef = new NodeRef(resultObject.toString());

            NodeInfo nodeInfo = this
                    .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

            writeJsonResponse(response, nodeInfo);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    new NodeRef(createNodeOptions.copyFrom),
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }

    }

    @ApiOperation("Moves a node by changing its parent")
    @Uri(value = "/nodes/{space}/{store}/{guid}/parent", method = HttpMethod.PUT)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void setParent(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, final ChangeParentOptions location, WebScriptResponse response)
            throws IOException {
        NodeRef parent = new NodeRef(location.parent);
        NodeRef nodeToMove = createNodeRef(space, store, guid);
        try {
            nodeService.moveNode(nodeToMove, parent);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    nodeToMove,
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
    }

    @ApiOperation(value = "Retrieves all comments for a given node")
    @Uri(value = "/nodes/{space}/{store}/{guid}/comments", method = HttpMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void getComments(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            @RequestParam(defaultValue = "0") int skipcount, @RequestParam(defaultValue = "10") int pagesize,
            WebScriptResponse response) throws IOException {
        final NodeRef target = this.createNodeRef(space, store, guid);
        if (nodeService.exists(target)) {
            if (permissionService.hasPermission(target, PermissionService.READ)) {
                Conversation comments = commentService.getComments(target, skipcount, pagesize);
                boolean canCreate = permissionService.hasPermission(target, PermissionService.CREATE_CHILDREN);
                comments.setCreatable(canCreate);
                response.setStatus(HttpStatus.SC_OK);
                writeJsonResponse(response, comments);
            } else {
                throw new AccessDeniedException("User does not have permission to read parent node");
            }
        } else {
            writeNotFoundResponse(response, target);
        }
    }

    @ApiOperation(value = "Appends a new comment to the given node.")
    @Uri(value = "/nodes/{space}/{store}/{guid}/comments", method = HttpMethod.POST)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void addComment(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final Comment newComment, WebScriptRequest request, WebScriptResponse response) throws IOException {
        final NodeRef target = new NodeRef(space, store, guid);
        if (nodeService.exists(target)) {
            Comment responseComment = commentService.addNewComment(target, newComment.getContent());
            response.setStatus(HttpStatus.SC_OK);
            writeJsonResponse(response, responseComment);
        } else {
            writeNotFoundResponse(response, target);
        }
    }

    @ApiOperation(value = "Returns the comment with the given id.")
    @Uri(value = "/comments/{space}/{store}/{guid}",
            method = HttpMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void getComment(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptRequest request, WebScriptResponse response) throws IOException {
        final NodeRef targetComment = new NodeRef(space, store, guid);
        if (nodeService.exists(targetComment)) {
            if (permissionService.hasPermission(targetComment, PermissionService.READ)) {
                Comment comment = commentService.getComment(targetComment);
                response.setStatus(HttpStatus.SC_OK);
                writeJsonResponse(response, comment);
            } else {
                throw new AccessDeniedException(String.format("User does not have permission " +
                        "to read the comment node %s", targetComment.toString()));
            }
        } else {
            writeNotFoundResponse(response, targetComment);
        }
    }

    @ApiOperation(value = "Updates the comment with the given id.")
    @Uri(value = "/comments/{space}/{store}/{guid}",
            method = HttpMethod.PUT)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void updateComment(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final Comment newComment, WebScriptRequest request, WebScriptResponse response) throws IOException {
        final NodeRef targetComment = new NodeRef(space, store, guid);
        if (nodeService.exists(targetComment)) {
            Comment updatedComment = commentService.updateComment(targetComment, newComment.getContent());
            response.setStatus(HttpStatus.SC_OK);
            writeJsonResponse(response, updatedComment);
        } else {
            writeNotFoundResponse(response, targetComment);
        }
    }

    @ApiOperation(value = "Deletes the comment with the given id.")
    @Uri(value = "/comments/{space}/{store}/{guid}",
            method = HttpMethod.DELETE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public void deleteComment(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptRequest request, WebScriptResponse response) throws IOException {
        final NodeRef targetComment = new NodeRef(space, store, guid);
        if (nodeService.exists(targetComment)) {
            commentService.deleteComment(targetComment);
            response.setStatus(HttpStatus.SC_OK);
            writeJsonResponse(response, String.format("Comment %s deleted", targetComment.toString()));
        } else {
            writeNotFoundResponse(response, targetComment);
        }
    }

    @ApiOperation(value = "Downloads content file for given node")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content", method = HttpMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")})
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    @ApiImplicitParams({@ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = false)})
    public void setContent(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final WebScriptRequest multiPart, WebScriptResponse response) throws IOException {
        final NodeRef finalDestination = this.createNodeRef(space, store, guid);
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

        final org.springframework.extensions.webscripts.servlet.FormData.FormField finalContent = content;
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {

                nodeService
                        .setContent(finalDestination, finalContent != null ? finalContent.getInputStream() : null,
                                finalContent != null ? finalContent.getFilename() : null);
                return null;
            }
        }, false, true);

        //writeJsonResponse(response, new Message("Successfully set content"));

    }


    @ApiOperation(value = "Sets the content for given node to empty")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content", method = HttpMethod.DELETE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public void exists(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            WebScriptResponse response) throws IOException {
        NodeRef nodeRef = new NodeRef(space, store, guid);
        writeJsonResponse(response, nodeService.exists(nodeRef));

    }


    @ApiOperation(value = "Creates a new node with given content", consumes = "multipart/form-data")
    @Uri(value = "/nodes/upload", method = HttpMethod.POST)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
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
        NodeRef resultRef = null;
        try {
            resultRef = transactionHelper
                    .doInTransaction(() -> createNodeForUpload(finalParent, finalFile, finalType, finalMetadata,
                            finalExtractMetadata), false, true);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    null,
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo((NodeRef) resultRef, fileFolderService, nodeService, permissionService);
        writeJsonResponse(response, nodeInfo);
    }

    public NodeRef createNodeForUpload(String finalParent,
            FormData.FormField finalFile,
            String finalType,
            MetadataChanges finalMetadata,
            Boolean finalExtractMetadata) {
        NodeRef newNode = nodeService
                .createNode(new NodeRef(finalParent), finalFile.getFilename(),
                        new QName(finalType));
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

    @ExceptionHandler(AccessDeniedException.class)
    private void writeNotAuthorizedResponse(AccessDeniedException exception, WebScriptResponse response)
            throws IOException {
        logger.debug("Not Authorized: ", exception);
        response.setStatus(403);
        writeJsonResponse(response, "Not authorised to execute this operation");
    }

    @ExceptionHandler(FileExistsException.class)
    private void writeFileExistsResponse(FileExistsException fileExistsException, WebScriptResponse response)
            throws IOException {
        String message = fileExistsException.toString();
        logger.debug(message);
        response.setStatus(400);
        writeJsonResponse(response, message);
    }

    private void writeNotFoundResponse(WebScriptResponse response, NodeRef requestedNode) throws IOException {
        String message = String.format("Node Not Found: %s", requestedNode);
        logger.debug(message);
        response.setStatus(404);
        writeJsonResponse(response, message);
    }
}
