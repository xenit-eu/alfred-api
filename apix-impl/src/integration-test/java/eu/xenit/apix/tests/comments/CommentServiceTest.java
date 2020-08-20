package eu.xenit.apix.tests.comments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.comments.Conversation;
import eu.xenit.apix.comments.ICommentService;
import eu.xenit.apix.tests.BaseTest;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentServiceTest extends BaseTest {

    private static final String commentTitle = "testComment";
    private static final String commentContent = "Test Comment";
    private static FileInfo testDocumentNode;

    @Autowired
    private ICommentService commentService;
    @Autowired
    private ApixToAlfrescoConversion apixConversion;
    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private CommentService alfrescoCommentService;

    @Before
    public void setupComments() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        cleanUp();
        NodeRef companyHomeRef = repository.getCompanyHome();
        FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);
        testDocumentNode = this.createTestNode(mainTestFolder.getNodeRef(), "testNode");
    }

    @Test
    public void test_getDocumentForComment() {
        NodeRef commentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        eu.xenit.apix.data.NodeRef documentApixNode = commentService
                .getTargetDocumentForComment(apixConversion.apix(commentNode));
        assertEquals(apixConversion.apix(testDocumentNode.getNodeRef()), documentApixNode);
    }

    @Test
    public void test_getComments_withHasMoreIsFalse() {
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();
        NodeRef commentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(apixConversion.apix(testDocumentNode.getNodeRef()), 0, 10);
        assertTrue(actual.isCreatable());
        assertFalse(actual.hasMore());
        assertEquals(1, actual.getComments().size());
        Comment apixComment = actual.getComments().get(0);
        assertEquals(apixConversion.apix(commentNode), apixComment.getId());
        assertEquals(commentTitle, apixComment.getTitle());
        assertEquals(commentContent, apixComment.getContent());
        Map<QName, Serializable> commentProperties = alfrescoNodeService.getProperties(commentNode);
        assertEquals(commentProperties.get(ContentModel.PROP_CREATOR), apixComment.getCreatedBy());
        assertEquals(commentProperties.get(ContentModel.PROP_MODIFIER), apixComment.getModifiedBy());
        assertTrue(apixComment.isEditable());
        assertTrue(apixComment.isDeletable());
    }

    @Test
    public void test_getComments_withHasMoreIsTrue_withPageSize() {
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(apixConversion.apix(testDocumentNode.getNodeRef()), 0, 1);
        assertTrue(actual.hasMore());
        assertEquals(1, actual.getComments().size());
    }

    @Test
    public void test_getComments_withSkipCount_withPageSize() {
        //order is of nodes chronologically descending (highest index = oldest node)
        NodeRef oldestNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(apixConversion.apix(testDocumentNode.getNodeRef()), 1, 1);
        assertEquals(1, actual.getComments().size());
        assertEquals(apixConversion.apix(oldestNode), actual.getComments().get(0).getId());
    }

    @Test
    public void test_addNewComment() {
        eu.xenit.apix.data.NodeRef apixTestDocNode = apixConversion.apix(testDocumentNode.getNodeRef());
        Comment newComment = commentService.addNewComment(apixTestDocNode, commentContent);
        Conversation actual = commentService.getComments(apixTestDocNode, 0, 10);
        assertEquals(1, actual.getComments().size());
        assertEquals(newComment, actual.getComments().get(0));
    }

    @Test
    public void test_updateComment() {
        NodeRef alfrescoCommentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        eu.xenit.apix.data.NodeRef apixCommentNode = apixConversion.apix(alfrescoCommentNode);
        String newCommentContent = "New Content";
        commentService.updateComment(apixCommentNode, newCommentContent);
        eu.xenit.apix.data.NodeRef apixTestDocNode = apixConversion.apix(testDocumentNode.getNodeRef());
        Conversation actual = commentService.getComments(apixTestDocNode, 0, 1);
        assertEquals(1, actual.getComments().size());
        assertEquals(newCommentContent, actual.getComments().get(0).getContent());
        assertEquals(apixCommentNode, actual.getComments().get(0).getId());
    }

    @Test
    public void test_deleteComment() {
        NodeRef alfrescoCommentNodeRef = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        commentService.deleteComment(apixConversion.apix(alfrescoCommentNodeRef));
        assertEquals(0,
                commentService.getComments(apixConversion.apix(testDocumentNode.getNodeRef()), 0, 10).getComments()
                        .size());
    }
}
