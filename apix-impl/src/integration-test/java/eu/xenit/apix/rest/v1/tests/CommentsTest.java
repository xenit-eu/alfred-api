package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.comments.Comment;
import eu.xenit.apix.comments.Conversation;
import eu.xenit.apix.comments.ICommentService;
import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentsTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CommentsTest.class);

    private static final String commentTitle = "commentTitle";
    private static final String commentContent = "Comment Content";
    private static final String COMMENTNODE1 = "testComment";
    private static final String COMMENTNODE_NORIGHTS = "testComment_noRights";
    private static final String SKIPCOUNT = "skipcount";
    private static final String PAGESIZE = "pagesize";

    @Autowired
    private CommentService alfrescoCommentService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private ApixToAlfrescoConversion apixConverter;

    @Autowired
    private NodeService nodeService;

    @Override
    public HashMap<String, NodeRef> init() {
        HashMap<String, NodeRef> response = super.init();
        response = initComments(response);
        return response;
    }

    private HashMap<String, NodeRef> initComments(HashMap<String, NodeRef> initializedNodeRefs) {
        org.alfresco.service.cmr.repository.NodeRef alfrescoCommentNode = alfrescoCommentService
                .createComment(apixConverter.alfresco(initializedNodeRefs.get(TESTFILE_NAME)), commentTitle,
                        commentContent, false);
        Comment comment = commentService.addNewComment(initializedNodeRefs.get(TESTFILE_NAME), commentContent);
        log.info("Created comment : " +  comment.getId());
        log.info("Exists : " + nodeService.exists(comment.getId()));
        initializedNodeRefs.put(COMMENTNODE1, comment.getId());
        org.alfresco.service.cmr.repository.NodeRef alfrescoCommentNode2 = alfrescoCommentService
                .createComment(apixConverter.alfresco(initializedNodeRefs.get(NOUSERRIGHTS_FILE_NAME)), commentTitle,
                        commentContent, false);
        initializedNodeRefs.put(COMMENTNODE_NORIGHTS, new NodeRef(alfrescoCommentNode2.toString()));
        return initializedNodeRefs;
    }

    @Test
    public void testGetComments() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(BaseTest.TESTFILE_NAME), "/comments", "admin", "admin");
        log.info("URL: {}", url);
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT,"0");
        //HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        //assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        HttpGet req = new HttpGet(builder.build());
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        log.info("URL: {}", url);

        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            String result = EntityUtils.toString(response.getEntity());
            log.info("Result mutiple : " + result);
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testGetComment() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(COMMENTNODE1), "admin", "admin");
        Conversation conversation = commentService.getComments(initializedRefs.get(COMMENTNODE1), 0, 10);
        log.info("Comments : " + conversation.getComments() );
        HttpGet req = new HttpGet(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        log.info("URL: {}", url);

        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            String result = EntityUtils.toString(response.getEntity());
            log.info("Result : " + result);
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testGetCommentsAccessDenied() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME), "/comments", BaseTest.USERWITHOUTRIGHTS,
                BaseTest.USERWITHOUTRIGHTS);
        log.info("URL: {}", url);
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT,"0");
        HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetCommentsNotFound() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(new NodeRef("workspace://SpacesStore/00000000-0000-0000-0000-000000000000"), "/comments", "admin", "admin");
        log.info("URL: {}", url);
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT,"0");
        HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        assertEquals(404, httpResponse.getStatusLine().getStatusCode());
    }

}