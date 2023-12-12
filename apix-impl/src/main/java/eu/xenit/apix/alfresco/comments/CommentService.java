package eu.xenit.apix.alfresco.comments;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.comments.Conversation;
import eu.xenit.apix.comments.ICommentService;
import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.IPermissionService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.apix.comments.CommentService")
public class CommentService implements ICommentService {
    private final Logger logger = LoggerFactory.getLogger(CommentService.class);

    protected org.alfresco.repo.forum.CommentService commentService;

    protected IContentService contentService;
    protected INodeService nodeService;
    protected IPermissionService permissionService;
    protected ApixToAlfrescoConversion apixConverter;

    @Autowired
    public CommentService(org.alfresco.repo.forum.CommentService commentService, IContentService contentService,
            INodeService nodeService, IPermissionService permissionService, ApixToAlfrescoConversion apixConverter) {
        this.commentService = commentService;

        this.contentService = contentService;
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.apixConverter = apixConverter;
    }

    @Override
    public NodeRef getTargetDocumentForComment(NodeRef commentNodeRef) {
        return apixConverter.apix(commentService.getDiscussableAncestor(apixConverter.alfresco(commentNodeRef)));
    }

    @Override
    public Conversation getComments(NodeRef targetNode, int skipCount, int pageSize) {
        org.alfresco.service.cmr.repository.NodeRef alfTargetNode = apixConverter.alfresco(targetNode);
        PagingResults<org.alfresco.service.cmr.repository.NodeRef> commentAlfNodes =
                commentService.listComments(alfTargetNode, new PagingRequest(skipCount, pageSize));
        return new Conversation(
                commentAlfNodes.getPage().stream().map(alfCommentNode -> toComment(alfTargetNode, alfCommentNode))
                        .collect(Collectors.toList()),
                commentAlfNodes.hasMoreItems(),
                canCreateComment(targetNode));
    }

    @Override
    public Comment getComment(NodeRef commentNode) {
        NodeRef alfTargetNode = getTargetDocumentForComment(commentNode);
        return toComment(apixConverter.alfresco(alfTargetNode), apixConverter.alfresco(commentNode));
    }

    @Override
    public Comment addNewComment(NodeRef targetNode, String content) {
        org.alfresco.service.cmr.repository.NodeRef alfTargetNode = apixConverter.alfresco(targetNode);
        return toComment(alfTargetNode, commentService.createComment(alfTargetNode, "", content, false));
    }

    @Override
    public Comment updateComment(NodeRef targetCommentNode, String content) {
        return updateComment(getTargetDocumentForComment(targetCommentNode), targetCommentNode, content);
    }

    @Override
    public Comment updateComment(NodeRef targetDocumentNode, NodeRef targetCommentNode, String content) {
        org.alfresco.service.cmr.repository.NodeRef alfTargetCommentNode = apixConverter.alfresco(targetCommentNode);
        commentService.updateComment(alfTargetCommentNode, "", content);
        org.alfresco.service.cmr.repository.NodeRef alfTargetDocumentNode = apixConverter.alfresco(targetDocumentNode);
        return toComment(alfTargetDocumentNode, alfTargetCommentNode);
    }

    @Override
    public void deleteComment(NodeRef targetCommentNode) {
        commentService.deleteComment(apixConverter.alfresco(targetCommentNode));
    }

    @Override
    public boolean canCreateComment(NodeRef targetNodeRef) {
        return permissionService.hasPermission(targetNodeRef, IPermissionService.CREATE_CHILDREN);
    }

    protected Comment toComment(org.alfresco.service.cmr.repository.NodeRef documentNode,
            org.alfresco.service.cmr.repository.NodeRef commentNodeRef) {
        NodeRef apixCommentNodeRef = apixConverter.apix(commentNodeRef);
        NodeMetadata commentMetadata = nodeService.getMetadata(apixCommentNodeRef);
        String content;
        try {
            content = IOUtils.toString(contentService.getContent(apixCommentNodeRef).getInputStream());
        } catch (IOException e) {
            String message = String.format("Encountered an IOException while handling comment %s", commentNodeRef);
            throw new AlfrescoRuntimeException(message, e);
        }
        Comment response = new Comment();
        response.setId(apixCommentNodeRef);
        response.setContent(content);
        List<String> property = commentMetadata.getProperties().get(new QName(ContentModel.PROP_TITLE.toString()));
        if (property != null && !property.isEmpty()) {
            response.setTitle(property.get(0));
        }
        property = commentMetadata.getProperties().get(new QName(ContentModel.PROP_CREATED.toString()));
        if (property != null && !property.isEmpty()) {
            response.setCreatedAt(property.get(0));
        }
        property = commentMetadata.getProperties().get(new QName(ContentModel.PROP_CREATOR.toString()));
        if (property != null && !property.isEmpty()) {
            response.setCreatedBy(property.get(0));
        }
        property = commentMetadata.getProperties().get(new QName(ContentModel.PROP_MODIFIED.toString()));
        if (property != null && !property.isEmpty()) {
            response.setModifiedAt(property.get(0));
        }
        property = commentMetadata.getProperties().get(new QName(ContentModel.PROP_MODIFIER.toString()));
        if (property != null && !property.isEmpty()) {
            response.setModifiedBy(property.get(0));
        }
        setPermissions(documentNode, commentNodeRef, response);
        return response;
    }

    protected Comment setPermissions(org.alfresco.service.cmr.repository.NodeRef documentNode,
            org.alfresco.service.cmr.repository.NodeRef commentNodeRef, Comment targetComment) {
        Map<String, Boolean> commentPermissionMap = commentService.getCommentPermissions(documentNode, commentNodeRef);
        targetComment.setEditable(commentPermissionMap.get(org.alfresco.repo.forum.CommentService.CAN_EDIT));
        targetComment.setDeletable(commentPermissionMap.get(org.alfresco.repo.forum.CommentService.CAN_DELETE));
        return targetComment;
    }
}
