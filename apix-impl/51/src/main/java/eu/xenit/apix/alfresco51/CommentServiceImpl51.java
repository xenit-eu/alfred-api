package eu.xenit.apix.alfresco51;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.comments.CommentService;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import java.io.Serializable;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.apix.comments.CommentService")
@OsgiService
public class CommentServiceImpl51 extends CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl51.class);

    protected LockService alfLockService;
    protected NodeService alfNodeService;
    protected PermissionService alfPermissionService;

    @Autowired
    public CommentServiceImpl51(
            LockService alfLockService,
            NodeService alfNodeService,
            PermissionService alfPermissionService,
            org.alfresco.repo.forum.CommentService commentService,
            IContentService contentService, INodeService nodeService,
            IPermissionService permissionService,
            ApixToAlfrescoConversion apixConverter) {
        super(commentService, contentService, nodeService, permissionService, apixConverter);
        this.alfLockService = alfLockService;
        this.alfNodeService = alfNodeService;
        this.alfPermissionService = alfPermissionService;
    }

    @Override
    protected Comment setPermissions(NodeRef documentNode,
            NodeRef commentNodeRef, Comment targetComment) {
        boolean canEdit = false;
        boolean canDelete = false;
        if (!isWorkingCopyOrLocked(documentNode)) {
            canEdit = canEditPermission(commentNodeRef);
            canDelete = canDeletePermission(commentNodeRef);
        }
        targetComment.setEditable(canEdit);
        targetComment.setDeletable(canDelete);
        return targetComment;
    }

    protected boolean isWorkingCopyOrLocked(NodeRef documentNode) {
        boolean isworkingcopy = false;
        boolean islocked = false;
        if (alfNodeService.exists(documentNode)) {
            Set<QName> documentAspects = alfNodeService.getAspects(documentNode);
            isworkingcopy = documentAspects.contains(ContentModel.ASPECT_WORKING_COPY);
            if (!isworkingcopy && documentAspects.contains(ContentModel.ASPECT_LOCKABLE)) {
                log.debug("Node is not a working copy");
                LockStatus lockStatus = alfLockService.getLockStatus(documentNode);
                islocked = lockStatus == LockStatus.LOCKED;
            }
        }
        return (isworkingcopy || islocked);
    }

    protected boolean canEditPermission(NodeRef commentNodeRef) {
        String creator = (String) alfNodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR);
        Serializable owner = alfNodeService.getProperty(commentNodeRef, ContentModel.PROP_OWNER);
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        boolean isSiteManager =
                alfPermissionService.hasPermission(commentNodeRef, SiteModel.SITE_MANAGER) == (AccessStatus.ALLOWED);
        boolean isCoordinator = alfPermissionService.hasPermission(commentNodeRef, PermissionService.COORDINATOR)
                == (AccessStatus.ALLOWED);
        return (isSiteManager || isCoordinator || currentUser.equals(creator) || currentUser.equals(owner));
    }

    protected boolean canDeletePermission(NodeRef commentNodeRef) {
        return alfPermissionService.hasPermission(commentNodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED;
    }
}
