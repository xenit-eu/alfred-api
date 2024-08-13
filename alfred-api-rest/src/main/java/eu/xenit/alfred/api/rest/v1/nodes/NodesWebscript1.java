package eu.xenit.alfred.api.rest.v1.nodes;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.comments.Comment;
import eu.xenit.alfred.api.comments.Conversation;
import eu.xenit.alfred.api.comments.ICommentService;
import eu.xenit.alfred.api.data.ContentInputStream;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.exceptions.FileExistsException;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.filefolder.NodePath;
import eu.xenit.alfred.api.node.ChildParentAssociation;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.MetadataChanges;
import eu.xenit.alfred.api.node.NodeAssociation;
import eu.xenit.alfred.api.node.NodeAssociations;
import eu.xenit.alfred.api.node.NodeMetadata;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.permissions.NodePermission;
import eu.xenit.alfred.api.permissions.PermissionValue;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import eu.xenit.alfred.api.rest.v1.nodes.ChangeAclsOptions.Access;
import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@AlfrescoTransaction
@RestController
public class NodesWebscript1 extends AlfredApiV1Webscript {

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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/metadata")
    public ResponseEntity<NodeMetadata> setMetadata(@PathVariable final String space, @PathVariable final String store,
            @PathVariable final String guid, @RequestBody final MetadataChanges changes) {
        NodeRef nodeRef = createNodeRef(space, store, guid);
        NodeMetadata nodeMetadata = nodeService.setMetadata(nodeRef, changes);
        if (nodeMetadata == null) {
            ResponseEntity.notFound();
        }
        return writeJsonResponse(nodeMetadata);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/metadata")
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

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}")
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
    public ResponseEntity<NodeAssociations> getAssociations(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
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

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations")
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/parents")
    public ResponseEntity<List<ChildParentAssociation>> getParentAssociations(@PathVariable String space,
            @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getParentAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/children")
    public ResponseEntity<List<ChildParentAssociation>> getChildAssociations(@PathVariable String space,
            @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getChildAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/associations/targets")
    public ResponseEntity<List<NodeAssociation>> getSourcePeerAssociations(@PathVariable String space,
            @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.nodeService.getTargetAssociations(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions")
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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}")
    public ResponseEntity<Void> setPermission(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @PathVariable String authority,
            @PathVariable String permission) {
        this.permissionService.setPermission(
                this.createNodeRef(space, store, guid), authority, permission
        );
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/permissions/authority/{authority}/permission/{permission}")
    public ResponseEntity<Void> deletePermission(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @PathVariable String authority,
            @PathVariable String permission) {
        this.permissionService.deletePermission(this.createNodeRef(space, store, guid), authority, permission);
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl")
    public ResponseEntity<NodePermission> getAcls(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.permissionService.getNodePermissions(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl/inheritFromParent")
    public void setInheritParentPermissions(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @RequestBody final InheritFromParent inherit) {
        this.permissionService.setInheritParentPermissions(
                this.createNodeRef(space, store, guid), inherit.isInheritFromParent()
        );
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/acl")
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/path")
    public ResponseEntity<NodePath> getPath(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                this.fileFolderService.getPath(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}")
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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/nodeInfo")
    public ResponseEntity<Object> getAllInfoOfNodes(@RequestBody final NodeInfoRequest nodeInfoRequest) {
        List<NodeInfo> nodeInfoList = this.nodeRefToNodeInfo(nodeInfoRequest,
                this.fileFolderService,
                this.nodeService,
                this.permissionService
        );
        return writeJsonResponse(nodeInfoList);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/ancestors")
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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes")
    public ResponseEntity<NodeInfo> createNode(@RequestBody final CreateNodeOptions createNodeOptions) {
        try {
            Object resultObject = serviceRegistry.getRetryingTransactionHelper()
                    .doInTransaction(() -> {
                        NodeRef parent = new NodeRef(createNodeOptions.getParent());

                        if (!nodeService.exists(parent)) {
                            return writeNotFoundResponse(parent);
                        }

                        NodeRef nodeRef;
                        NodeRef copyFrom = null;
                        if (createNodeOptions.getCopyFrom() == null) {
                            nodeRef = nodeService
                                    .createNode(parent, createNodeOptions.getName(),
                                            new QName(createNodeOptions.getType()));
                        } else {
                            copyFrom = new NodeRef(createNodeOptions.getCopyFrom());
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
                            type = nodeService.getMetadata(copyFrom).getType();
                        } else {
                            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
                                    .body("Please provide parameter \"type\" when creating a new node");
                        }

                        metadataChanges = new MetadataChanges(type,
                                createNodeOptions.getAspectsToAdd(),
                                createNodeOptions.getAspectsToRemove(),
                                createNodeOptions.getProperties());
                        nodeService.setMetadata(nodeRef, metadataChanges);

                        return nodeRef;
                    }, false, true);

            if (resultObject == null) {
                return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
            }
            NodeRef resultRef = new NodeRef(resultObject.toString());

            NodeInfo nodeInfo = this
                    .nodeRefToNodeInfo(resultRef, this.fileFolderService, this.nodeService, this.permissionService);

            return writeJsonResponse(nodeInfo);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    new NodeRef(createNodeOptions.getCopyFrom()),
                    new NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/parent")
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/comments")
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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/{space}/{store}/{guid}/comments")
    public ResponseEntity<?> addComment(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @RequestBody final Comment newComment) {
        final NodeRef target = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(target)) {
            return writeNotFoundResponse(target);
        }
        Comment responseComment = commentService.addNewComment(target, newComment.getContent());
        return writeJsonResponse(responseComment);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/comments/{space}/{store}/{guid}")
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

    @AlfrescoTransaction
    @PutMapping(value = "/v1/comments/{space}/{store}/{guid}")
    public ResponseEntity<?> updateComment(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid, @RequestBody final Comment newComment) {
        final NodeRef targetComment = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(targetComment)) {
            return writeNotFoundResponse(targetComment);
        }
        Comment updatedComment = commentService.updateComment(targetComment, newComment.getContent());
        return writeJsonResponse(updatedComment);
    }

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/comments/{space}/{store}/{guid}")
    public ResponseEntity<?> deleteComment(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        final NodeRef targetComment = this.createNodeRef(space, store, guid);
        if (!nodeService.exists(targetComment)) {
            return writeNotFoundResponse(targetComment);
        }
        commentService.deleteComment(targetComment);
        return writeJsonResponse(String.format("Comment %s deleted", targetComment));
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
    public ResponseEntity<?> getContent(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        final NodeRef nodeRef = this.createNodeRef(space, store, guid);
        ContentInputStream contentInputStream = nodeService.getContent(nodeRef);
        if (contentInputStream == null) {
            return writeNotFoundResponse(nodeRef);
        }
        return ResponseEntity.ok()
                .body(new InputStreamResource(contentInputStream.getInputStream()));
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
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

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/nodes/{space}/{store}/{guid}/content")
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable String space, @PathVariable String store,
            @PathVariable String guid) {
        return writeJsonResponse(
                nodeService.exists(
                        this.createNodeRef(space, store, guid)
                )
        );
    }

    @AlfrescoTransaction
    @PostMapping(value = "/v1/nodes/upload")
    public ResponseEntity<NodeInfo> uploadNode(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String parent,
            @RequestParam(required = false) Boolean extractMetadata,
            @RequestPart(required = false) MetadataChanges metadata,
            @RequestPart final MultipartFile file) {
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        // Note the difference between:
        //  * metadata        = The metadata the user annotates the file with.
        //  * extractMetadata = Whether the users wants metadata automatically extracted from the file.
        // Both setting metadata and extracting metadata are optional.
        // They can happen (or not) independently from each other.
        type = type == null ? ContentModel.TYPE_CONTENT.toString() : type;
        extractMetadata = Boolean.TRUE.equals(extractMetadata);

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
    private ResponseEntity<String> writeNotAuthorizedResponse(AccessDeniedException ex) {
        logger.debug("Not Authorized", ex);
        return ResponseEntity
                .status(HttpStatus.SC_FORBIDDEN)
                .body("Not authorised to execute this operation");
    }

    @ExceptionHandler(FileExistsException.class)
    private ResponseEntity<String> writeFileExistsResponse(FileExistsException fileExistsException) {
        String message = fileExistsException.toString();
        logger.debug(message, fileExistsException);
        return ResponseEntity
                .status(HttpStatus.SC_BAD_REQUEST)
                .body(message);
    }

    private ResponseEntity<String> writeNotFoundResponse(NodeRef requestedNode) {
        String message = String.format("Node Not Found: %s", requestedNode);
        logger.debug(message);
        return ResponseEntity
                .status(HttpStatus.SC_NOT_FOUND)
                .body(message);
    }
}
