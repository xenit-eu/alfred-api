package eu.xenit.alfred.api.tests.comments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.comments.Comment;
import eu.xenit.alfred.api.comments.Conversation;
import eu.xenit.alfred.api.comments.ICommentService;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class CommentServiceTest extends JavaApiBaseTest {

    private static final String commentTitle = "testComment";
    private static final String commentContent = "Test Comment";
    private static FileInfo testDocumentNode;

    private final ICommentService commentService;
    private final CommentService alfrescoCommentService;

    public CommentServiceTest() {
        commentService = getBean(ICommentService.class);
        alfrescoCommentService = getBean("CommentService", CommentService.class);
    }

    @Before
    public void setupComments() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        cleanUp();
        NodeRef companyHomeRef = repository.getCompanyHome();
        FileInfo mainTestFolder = createMainTestFolder(companyHomeRef);
        testDocumentNode = this.createTestNode(mainTestFolder.getNodeRef(), "testNode");
    }

    @Test
    public void testGetDocumentForComment() {
        NodeRef commentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        eu.xenit.alfred.api.data.NodeRef documentApixNode = commentService
                .getTargetDocumentForComment(c.apix(commentNode));
        assertEquals(c.apix(testDocumentNode.getNodeRef()), documentApixNode);
    }

    @Test
    public void testGetCommentsWithHasMoreIsFalse() {
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();
        NodeRef commentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(c.apix(testDocumentNode.getNodeRef()), 0, 10);
        assertTrue(actual.isCreatable());
        assertFalse(actual.hasMore());
        assertEquals(1, actual.getComments().size());
        Comment apixComment = actual.getComments().get(0);
        assertEquals(c.apix(commentNode), apixComment.getId());
        assertEquals(commentTitle, apixComment.getTitle());
        assertEquals(commentContent, apixComment.getContent());
        Map<QName, Serializable> commentProperties = alfrescoNodeService.getProperties(commentNode);
        assertEquals(commentProperties.get(ContentModel.PROP_CREATOR), apixComment.getCreatedBy());
        assertEquals(commentProperties.get(ContentModel.PROP_MODIFIER), apixComment.getModifiedBy());
        assertTrue(apixComment.isEditable());
        assertTrue(apixComment.isDeletable());
    }

    @Test
    public void testFetCommentsWithHasMoreIsTrueWithPageSize() {
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(c.apix(testDocumentNode.getNodeRef()), 0, 1);
        assertTrue(actual.hasMore());
        assertEquals(1, actual.getComments().size());
    }

    @Test
    public void testGetCommentsWithSkipCountWithPageSize() {
        //order is of nodes chronologically descending (highest index = oldest node)
        NodeRef oldestNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        alfrescoCommentService.createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        Conversation actual = commentService.getComments(c.apix(testDocumentNode.getNodeRef()), 1, 1);
        assertEquals(1, actual.getComments().size());
        assertEquals(c.apix(oldestNode), actual.getComments().get(0).getId());
    }

    @Test
    public void testAddNewComment() {
        eu.xenit.alfred.api.data.NodeRef apixTestDocNode = c.apix(testDocumentNode.getNodeRef());
        Comment newComment = commentService.addNewComment(apixTestDocNode, commentContent);
        Conversation actual = commentService.getComments(apixTestDocNode, 0, 10);
        assertEquals(1, actual.getComments().size());
        assertEquals(newComment, actual.getComments().get(0));
    }

    @Test
    public void testUpdateComment() {
        NodeRef alfrescoCommentNode = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        eu.xenit.alfred.api.data.NodeRef apixCommentNode = c.apix(alfrescoCommentNode);
        String newCommentContent = "New Content";
        commentService.updateComment(apixCommentNode, newCommentContent);
        eu.xenit.alfred.api.data.NodeRef apixTestDocNode = c.apix(testDocumentNode.getNodeRef());
        Conversation actual = commentService.getComments(apixTestDocNode, 0, 1);
        assertEquals(1, actual.getComments().size());
        assertEquals(newCommentContent, actual.getComments().get(0).getContent());
        assertEquals(apixCommentNode, actual.getComments().get(0).getId());
    }

    @Test
    public void testDeleteComment() {
        NodeRef alfrescoCommentNodeRef = alfrescoCommentService
                .createComment(testDocumentNode.getNodeRef(), commentTitle, commentContent, false);
        commentService.deleteComment(c.apix(alfrescoCommentNodeRef));
        assertEquals(0,
                commentService.getComments(c.apix(testDocumentNode.getNodeRef()), 0, 10).getComments()
                        .size());
    }
}
