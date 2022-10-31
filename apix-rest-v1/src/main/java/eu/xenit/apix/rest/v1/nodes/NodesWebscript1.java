package eu.xenit.apix.rest.v1.nodes;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
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
import eu.xenit.apix.rest.v1.nodes.ChangeAclsOptions.Access;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@AlfrescoTransaction
@RestController("eu.xenit.apix.rest.v1.NodesWebscript")
public class NodesWebscript1 extends ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(NodesWebscript1.class);

    private final INodeService nodeService;

    private final IPermissionService permissionService;

    private final IFileFolderService fileFolderService;

    private final ICommentService commentService;

    private final ServiceRegistry serviceRegistry;

    public NodesWebscript1(INodeService nodeService, IPermissionService permissionService,
                           IFileFolderService fileFolderService, ICommentService commentService,
                           ServiceRegistry serviceRegistry) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.fileFolderService = fileFolderService;
        this.commentService = commentService;
        this.serviceRegistry = serviceRegistry;
    }

    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/metadata")
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
                    "Changing the cm:name property will also update the qname path " +
                    "of the node so it is in sync with it.\n"
                    +
                    "This only applies to nodes of type or subtype cm:content or cm:folder " +
                    "but not of type or subtype of cm:systemfolder.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeMetadata.class),
            @ApiResponse(code = 403, message = "Not Authorized")
    })
    public ResponseEntity<NodeMetadata> setMetadata(@PathVariable final String space, @PathVariable final String store,
                            @PathVariable final String guid, @RequestBody final MetadataChanges changes) {
        NodeRef nodeRef = createNodeRef(space, store, guid);
        NodeMetadata nodeMetadata = nodeService.setMetadata(nodeRef, changes);
        if (nodeMetadata == null) {
           ResponseEntity.notFound();
        }
        return writeJsonResponse(nodeMetadata);
    }

    @ApiOperation("Retrieve node metadata")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/metadata")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeMetadata.class),
            @ApiResponse(code = 403, message = "Not Authorized")
    })
    public ResponseEntity<NodeMetadata> getMetadata(@PathVariable String space, @PathVariable String store,
                                                    @PathVariable String guid) {
        NodeMetadata nodeMetadata = nodeService.getMetadata(
                this.createNodeRef(space, store, guid)
        );
        if (nodeMetadata == null) {
            return ResponseEntity.notFound().build();
        } else {
            return writeJsonResponse(nodeMetadata);
        }
    }


    @ApiOperation("Delete a node")
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<String> deleteNode(@PathVariable final String space, @PathVariable final String store,
                                             @PathVariable final String guid,
                                             @RequestParam(required = false) final String permanently) {
        logger.debug(" permanently: {}", permanently);
        boolean deletePermanently = permanently != null && permanently.equals("true");
        logger.debug(" deletePermanently: {}", deletePermanently);
        NodeRef nodeRef = createNodeRef(space, store, guid);
        if (nodeService.deleteNode(nodeRef, deletePermanently)) {
            logger.debug("node {} deleted", nodeRef);
            return ResponseEntity.ok("Node deleted.");
        }
        logger.debug("Failed to delete node, node does not exist: {}", nodeRef);
        return ResponseEntity
                .status(HttpStatus.SC_NOT_FOUND)
                .body(String.format("Failed to delete node, node does not exist: %s", nodeRef.toString()));
    }

    @ApiOperation("Retrieve node associations.\n" +
            "Versionstore does not support sourceAssocs. " +
            "For version nodes, an empty list is returned for this component of the result.")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeAssociations.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<NodeAssociations> getAssociations(@PathVariable String space, @PathVariable String store,
                                                            @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation("Create new association with given node as source")
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> createAssociation(@PathVariable String space, @PathVariable String store,
                                                  @PathVariable String guid,
                                                  @RequestBody CreateAssociationOptions options) {
        nodeService.createAssociation(
                this.createNodeRef(space, store, guid),
                options.getTarget(),
                options.getType()
        );
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Deletes an association with given node as source")
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> deleteAssociation(@PathVariable String space, @PathVariable String store,
                                                  @PathVariable String guid, @RequestParam NodeRef target,
                                                  @RequestParam String type) {
        nodeService.removeAssociation(
                this.createNodeRef(space, store, guid),
                target,
                new QName(type)
        );
        return ResponseEntity.ok().build();
    }


    @ApiOperation("Retrieve node parent associations")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/parents")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<List<ChildParentAssociation>> getParentAssociations(@PathVariable String space,
                                                                              @PathVariable String store,
                                                                              @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getParentAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation("Retrieve node child associations")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/children")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = ChildParentAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<List<ChildParentAssociation>> getChildAssociations(@PathVariable String space,
                                                                             @PathVariable String store,
                                                                             @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getChildAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation("Retrieve node peer associations with given node being the source")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/targets")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeAssociation[].class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<List<NodeAssociation>> getSourcePeerAssociations(@PathVariable String space,
                                                                           @PathVariable String store,
                                                                           @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getTargetAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation(value = "Retrieve current user's permissions for a node",
            notes = "Returns a key-value map of permissions keys to a value of 'DENY' or 'ALLOW'. " +
                    "Possible keys are: Read, Write, Delete, CreateChildren, ReadPermissions, ChangePermissions, " +
                    "or custom permissions")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = PermissionValue.class, responseContainer = "Map")})
    // It would seem permissions can always be retrieved?
    public ResponseEntity<Map<String, PermissionValue>> getPermissions(@PathVariable String space,
                                                                       @PathVariable String store,
                                                                       @PathVariable String guid) {
        return writeJsonResponse(
                this.permissionService.getPermissions(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation(value = "sets a user a permission for a node.")
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> setPermission(@PathVariable String space, @PathVariable String store,
                                              @PathVariable String guid, @PathVariable String authority,
                                              @PathVariable String permission) {
        this.permissionService.setPermission(
                this.createNodeRef(space, store, guid), authority, permission
        );
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "removes a user its permission for a node.")
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> deletePermission(@PathVariable String space, @PathVariable String store,
                                                 @PathVariable String guid, @PathVariable String authority,
                                                 @PathVariable String permission) {
        this.permissionService.deletePermission(this.createNodeRef(space, store, guid), authority, permission);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Gets the ACLs for a node.")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodePermission.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<NodePermission> getAcls(@PathVariable String space, @PathVariable String store,
                                                  @PathVariable String guid) {
        return writeJsonResponse(
                this.permissionService.getNodePermissions(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl/inheritFromParent")
    @ApiResponses({@ApiResponse(code = 403, message = "Not Authorized")})
    public void setInheritParentPermissions(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @RequestBody final InheritFromParent inherit) {
        this.permissionService.setInheritParentPermissions(
                this.createNodeRef(space, store, guid), inherit.isInheritFromParent()
        );
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
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> setAcls(@PathVariable String space, @PathVariable String store,
                                        @PathVariable String guid,
                                        @RequestBody final ChangeAclsOptions changeAclsOptions) {
        NodePermission permissions = new NodePermission();
        permissions.setInheritFromParent(changeAclsOptions.isInheritFromParent());
        Set<NodePermission.Access> accessList = new HashSet<>();
        permissions.setOwnAccessList(accessList);
        Set<ChangeAclsOptions.Access> ownAccessList = changeAclsOptions.getOwnAccessList();
        if (ownAccessList == null) {
            return ResponseEntity.badRequest().build();
        }
        for (Access access : changeAclsOptions.getOwnAccessList()) {
            NodePermission.Access a = new NodePermission.Access();
            accessList.add(a);
            a.setAllowed(access.allowed);
            a.setAuthority(access.authority);
            a.setPermission(access.permission);
        }
        this.permissionService.setNodePermissions(
                this.createNodeRef(space, store, guid), permissions
        );
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Returns path of the node")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/path")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodePath.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<NodePath> getPath(@PathVariable String space, @PathVariable String store,
                                            @PathVariable String guid) {
        return writeJsonResponse(
                this.fileFolderService.getPath(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @ApiOperation("Returns combined information of a node.\n" +
            "Note: versionstore does not support sourceAssocs. " +
            "For version nodes, an empty list added to the result")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<Object> getAllInfoOfNode(@PathVariable String space, @PathVariable String store,
                                                   @PathVariable String guid) {
        try {
            // This method will return a null result if user has insufficient permissions
            NodeInfo nodeInfo = this.nodeRefToNodeInfo(
                    this.createNodeRef(space, store, guid),
                    this.fileFolderService, this.nodeService, this.permissionService
            );
            if (nodeInfo != null) {
                return writeJsonResponse(nodeInfo);
            }
            return ResponseEntity
                    .status(HttpStatus.SC_UNAUTHORIZED)
                    .body(String.format("Insufficient permissions for node %s://%s/%s", space, store, guid));
        } catch (InvalidNodeRefException invalidNodeRefException) {
            logger.debug("Failed to get node info, node does not exist: {}://{}/{}", space, store, guid,
                        invalidNodeRefException);
            return ResponseEntity
                    .status(HttpStatus.SC_NOT_FOUND)
                    .body("Failed to get node info, node does not exist.");
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
                    "'retrieveMetadata', 'retrievePath', 'retrievePermissions', " +
                    "'retrieveAssocs', 'retrieveChildAssocs',\n"
                    +
                    "'retrieveParentAssocs', 'retrieveTargetAssocs' are optional parameters.\n" +
                    "Set 'retrieveMetadata' to false to omit the aspects and properties from the result.\n" +
                    "Set 'retrievePath' to false to omit the path from the result.\n" +
                    "Set 'retrievePermissions' to false to omit the permissions from the result.\n" +
                    "Set 'retrieveAssocs' to false to omit the associations(parent associations, child associations, " +
                    "peer associations) from the result.\n"
                    +
                    "Set 'retrieveChildAssocs' to false to omit the child associations from the result.\n" +
                    "Set 'retrieveParentAssocs' to false to omit the parent associations from the result.\n" +
                    "Set 'retrieveTargetAssocs' to false to omit the peer target associations from the result.\n" +
                    "Set 'retrieveSourceAssocs' to false to omit the peer source associations from the result. " +
                    "Note: versionstore does not support sourceAssocs. " +
                    "For version nodes, an empty list added to the result\n")
    @PostMapping(value = "/v1/nodes/nodeInfo")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NodeInfo[].class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    // TODO @Zlatin MVC Pojo
    public ResponseEntity<Object> getAllInfoOfNodes(@RequestBody final String requestString) {
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

        List<NodeRef> nodeRefs = new ArrayList<>();
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
            return ResponseEntity.badRequest().build();
        }

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

        return writeJsonResponse(nodeInfoList);
    }

    @ApiOperation(value = "Retrieves the ancestors of the nodes",
            notes = "It is possible to add \"root\" as a request parameter.\n"
                    + "It is the node reference up to which point ancestors will be retrieved.\n"
                    + "The default root reference will be the reference of Company Home")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/ancestors")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = AncestorsObject.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Object> retrieveAncestors(@PathVariable String space, @PathVariable String store,
                                                    @PathVariable String guid,
                                                    @RequestParam(required = false) String root) {
        NodeRef nodeRef = this.createNodeRef(space, store, guid);
        NodeRef rootRef = null;
        if (root != null) {
            rootRef = new NodeRef(root);
        }

        try {
            List<NodeRef> ancestors = nodeService.getAncestors(nodeRef, rootRef);
            AncestorsObject ancestorsObject = new AncestorsObject(nodeRef, ancestors);
            return writeJsonResponse(ancestorsObject);
        } catch (InvalidNodeRefException ex) {
            logger.error("noderef does not exist", ex);
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
                            .body(ex.getMessage());
        } catch (AccessDeniedException ex) {
            logger.error("access denied on noderef", ex);
            return ResponseEntity.status(HttpStatus.SC_FORBIDDEN)
                    .body(ex.getMessage());
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
    @PostMapping(value = "/v1/nodes")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<NodeInfo> createNode(@RequestBody final CreateNodeOptions createNodeOptions) {
        try {
            Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                    .doInTransaction(() -> {
                        NodeRef parent = new NodeRef(createNodeOptions.parent);

                        if (!nodeService.exists(parent)) {
                            return writeNotFoundResponse(parent);
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
                                return writeNotFoundResponse(copyFrom);
                            }
                            nodeRef = nodeService.copyNode(copyFrom, parent, true);
                        }

                        MetadataChanges metadataChanges;
                        QName type;
                        if (createNodeOptions.getType() != null) {
                            type = new QName(createNodeOptions.getType());
                        } else if (createNodeOptions.getType() == null && createNodeOptions.getCopyFrom() != null) {
                            type = nodeService.getMetadata(copyFrom).type;
                        } else {
                            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
                                    .body("Please provide parameter \"type\" when creating a new node");
                        }

                        metadataChanges = new MetadataChanges(type,
                                createNodeOptions.aspectsToAdd,
                                createNodeOptions.aspectsToRemove,
                                createNodeOptions.properties);
                        nodeService.setMetadata(nodeRef, metadataChanges);

                        return nodeRef;
                    }, false, true);

            if (resultObject == null) {
                return ResponseEntity.internalServerError().build();
            }
            NodeRef resultRef = new NodeRef(resultObject.toString());

            NodeInfo nodeInfo = this
                    .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

            return writeJsonResponse(nodeInfo);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    new NodeRef(createNodeOptions.copyFrom),
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
    }

    @ApiOperation("Moves a node by changing its parent")
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/parent")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<NodeRef> setParent(@PathVariable final String space, @PathVariable final String store,
                                        @PathVariable final String guid, @RequestBody ChangeParentOptions location) {
        NodeRef nodeToMove = createNodeRef(space, store, guid);
        try {
            return ResponseEntity.ok(
                nodeService.moveNode(
                    nodeToMove,
                    new NodeRef(location.getParent())
                )
            );
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    nodeToMove,
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
    }

    @ApiOperation(value = "Retrieves all comments for a given node")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/comments")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<?> getComments(@PathVariable String space, @PathVariable String store,
                                             @PathVariable String guid, @RequestParam(defaultValue = "0") int skipcount,
                                             @RequestParam(defaultValue = "10") int pagesize) {
        final NodeRef target = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(target)) {
            return writeNotFoundResponse(target);
        }
        if (permissionService.hasPermission(target, PermissionService.READ)) {
            Conversation comments = commentService.getComments(target, skipcount, pagesize);
            boolean canCreate = permissionService.hasPermission(target, PermissionService.CREATE_CHILDREN);
            comments.setCreatable(canCreate);
            return writeJsonResponse(comments);
        }
        return writeNotAuthorizedResponse(new AccessDeniedException(target.getValue()));
    }

    @ApiOperation(value = "Appends a new comment to the given node.")
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/comments")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<?> addComment(@PathVariable String space, @PathVariable String store,
                                              @PathVariable String guid, @RequestBody final Comment newComment) {
        final NodeRef target = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(target)) {
            return writeNotFoundResponse(target);
        }
        Comment responseComment = commentService.addNewComment(target, newComment.getContent());
        return writeJsonResponse(responseComment);
    }

    @ApiOperation(value = "Returns the comment with the given id.")
    @GetMapping(value = "/v1/comments/{space}/{store}/{guid}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<?> getComment(@PathVariable String space, @PathVariable String store,
                                        @PathVariable String guid) {
        final NodeRef targetComment = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(targetComment)) {
            return writeNotFoundResponse(targetComment);
        }
        if (permissionService.hasPermission(targetComment, PermissionService.READ)) {
            return writeJsonResponse(commentService.getComment(targetComment));
        } else {
            throw new AccessDeniedException(String.format("User does not have permission " +
                    "to read the comment node %s", targetComment.toString()));
        }
    }

    @ApiOperation(value = "Updates the comment with the given id.")
    @PutMapping(value = "/v1/comments/{space}/{store}/{guid}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<?> updateComment(@PathVariable String space, @PathVariable String store,
                                           @PathVariable String guid, @RequestBody final Comment newComment) {
        final NodeRef targetComment = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(targetComment)) {
            return writeNotFoundResponse(targetComment);
        }
        Comment updatedComment = commentService.updateComment(targetComment, newComment.getContent());
        return writeJsonResponse(updatedComment);
    }

    @ApiOperation(value = "Deletes the comment with the given id.")
    @DeleteMapping(value = "/v1/comments/{space}/{store}/{guid}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<?> deleteComment(@PathVariable String space, @PathVariable String store,
                                           @PathVariable String guid) {
        final NodeRef targetComment = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(targetComment)) {
            return writeNotFoundResponse(targetComment);
        }
        commentService.deleteComment(targetComment);
        return writeJsonResponse(String.format("Comment %s deleted", targetComment));
    }

    @ApiOperation(value = "Downloads content file for given node")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<?> getContent(@PathVariable String space, @PathVariable String store, @PathVariable String guid) {
        final NodeRef nodeRef = this.createNodeRef(space, store, guid);
        ContentInputStream contentInputStream = nodeService.getContent(nodeRef);
        if (contentInputStream == null) {
            return writeNotFoundResponse(nodeRef);
        }
        return ResponseEntity.ok()
                .body(new InputStreamResource(contentInputStream.getInputStream()));
    }

    @ApiOperation(value = "Sets or updates the content for given node. " +
            "If no file is given the content will be set to empty.", consumes = "multipart/form-data")
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    @ApiImplicitParams({@ApiImplicitParam(name = "file", paramType = "form", dataType = "file")})
    public ResponseEntity<Void> setContent(@PathVariable String space, @PathVariable String store,
                                           @PathVariable String guid, @RequestPart final MultipartFile file) {
        final NodeRef finalDestination = this.createNodeRef(space, store, guid);
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        transactionHelper.doInTransaction(() -> {
            nodeService
                    .setContent(finalDestination, file != null ? file.getInputStream() : null,
                            file != null ? file.getOriginalFilename() : null);
            return null;
        }, false, true);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Sets the content for given node to empty")
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Void> deleteContent(@PathVariable String space, @PathVariable String store,
                                              @PathVariable String guid) {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        final NodeRef finalDestination = this.createNodeRef(space, store, guid);
        transactionHelper.doInTransaction(() -> {
            nodeService.setContent(finalDestination, null, null);
            return null;
        }, false, true);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "Checks if the given node exists")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/exists")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not Authorized")})
    public ResponseEntity<Boolean> exists(@PathVariable String space, @PathVariable String store,
                                          @PathVariable String guid) {
        return writeJsonResponse(
                nodeService.exists(
                        this.createNodeRef(space, store, guid)
                )
        );
    }


    @ApiOperation(value = "Creates a new node with given content", consumes = "multipart/form-data")
    @PostMapping(value = "/v1/nodes/upload")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = NodeInfo.class),
            @ApiResponse(code = 403, message = "Not Authorized")})
    @ApiImplicitParams({
            @ApiImplicitParam(value = "Noderef of parent for the new node", name = "parent", paramType = "form",
                    dataType = "string", required = true),
            //TODO: Datatype doesnt work here?
            @ApiImplicitParam(value = "QName type for the new node", name = "type", paramType = "form",
                    dataType = "string"),
            @ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = true),
            @ApiImplicitParam(value = "Metadata for this file", name = "metadata", paramType = "form",
                    dataType = "string"),
            @ApiImplicitParam(value = "Enable metadata extraction from the content, for example for msg files",
                    name = "extractMetadata", paramType = "form", dataType = "boolean"),
    })
    // TODO @Zlatin MVC Pojo
    public ResponseEntity<NodeInfo> uploadNode(
            @RequestPart(required = false) String type,
            @RequestPart(required = false) String parent,
            @RequestPart(required = false) Boolean extractMetadata,
            @RequestPart(required = false) MetadataChanges metadata,
            @RequestPart final MultipartFile file) {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        // Note the difference between:
        //  * metadata        = The metadata the user annotates the file with.
        //  * extractMetadata = Whether the users wants metadata automatically extracted from the file.
        // Both setting metadata and extracting metadata are optional.
        // They can happen (or not) independently from each other.
        type = type == null ? ContentModel.TYPE_CONTENT.toString() : type;
        extractMetadata = extractMetadata != null && extractMetadata;

        if (file == null) {
            throw new IllegalArgumentException("Content must be supplied as a multipart 'file' field");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Must supply a 'parent' field");
        }

        final String finalParent = parent;
        final String finalType = type;
        final MetadataChanges finalMetadata = metadata;
        final Boolean finalExtractMetadata = extractMetadata;
        NodeRef resultRef;
        try {
            resultRef = transactionHelper
                    .doInTransaction(() -> createNodeForUpload(finalParent, file, finalType, finalMetadata,
                            finalExtractMetadata), false, true);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    null,
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
        NodeInfo nodeInfo = this
                .nodeRefToNodeInfo(resultRef, fileFolderService, nodeService, permissionService);
        return writeJsonResponse(nodeInfo);
    }

    public NodeRef createNodeForUpload(String finalParent,
            MultipartFile file,
            String finalType,
            MetadataChanges finalMetadata,
            Boolean finalExtractMetadata) throws IOException {
        NodeRef newNode = nodeService
                .createNode(new NodeRef(finalParent), file.getOriginalFilename(),
                        new QName(finalType));
        nodeService.setContent(newNode, file.getInputStream(), file.getOriginalFilename());

        if (finalMetadata != null) {
            nodeService.setMetadata(newNode, finalMetadata);
        }

        if (Boolean.TRUE.equals(finalExtractMetadata)) {
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
    private ResponseEntity<Object> writeNotAuthorizedResponse(AccessDeniedException ex) {
        logger.debug("Not Authorized", ex);
        return ResponseEntity
                .status(HttpStatus.SC_FORBIDDEN)
                .body("Not authorised to execute this operation");
    }

    @ExceptionHandler(FileExistsException.class)
    private ResponseEntity<Object> writeFileExistsResponse(FileExistsException fileExistsException) {
        String message = fileExistsException.toString();
        logger.debug(message, fileExistsException);
        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(message);
    }

    private ResponseEntity<Object> writeNotFoundResponse(NodeRef requestedNode) {
        String message = String.format("Node Not Found: %s", requestedNode);
        logger.debug(message);
        return ResponseEntity
                .status(HttpStatus.SC_NOT_FOUND)
                .body(message);
    }
}
