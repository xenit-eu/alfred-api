package eu.xenit.apix.alfresco52;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.comments.CommentService;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.apix.comments.CommentService")
@OsgiService
public class CommentServiceImpl52 extends CommentService {

    @Autowired
    public CommentServiceImpl52(org.alfresco.repo.forum.CommentService commentService,
            IContentService contentService, INodeService nodeService,
            IPermissionService permissionService,
            ApixToAlfrescoConversion apixConverter) {
        super(commentService, contentService, nodeService, permissionService, apixConverter);
    }

    @Override
    protected Comment setPermissions(NodeRef documentNode, NodeRef commentNodeRef, Comment targetComment) {
        Map<String, Boolean> commentPermissionMap = commentService.getCommentPermissions(documentNode, commentNodeRef);
        targetComment.setEditable(commentPermissionMap.get(org.alfresco.repo.forum.CommentService.CAN_EDIT));
        targetComment.setDeletable(commentPermissionMap.get(org.alfresco.repo.forum.CommentService.CAN_DELETE));
        return targetComment;
    }
}
