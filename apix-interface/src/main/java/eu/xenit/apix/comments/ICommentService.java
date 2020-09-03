package eu.xenit.apix.comments;

import eu.xenit.apix.data.NodeRef;

public interface ICommentService {

    NodeRef getTargetDocumentForComment(NodeRef commentNodeRef);
    Conversation getComments(NodeRef targetNode, int skipCount, int pageSize);
    Comment getComment(NodeRef commentNode);
    Comment addNewComment(NodeRef targetNode, String content);
    Comment updateComment(NodeRef targetCommentNode, String content);
    Comment updateComment(NodeRef documentTargetNode, NodeRef commentTargetNode, String content);
    void deleteComment(NodeRef targetCommentNode);
    boolean canCreateComment(NodeRef targetDocumentNode);
}
