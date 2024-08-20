package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.alfresco.metadata.NodeService;
import eu.xenit.alfred.api.comments.Comment;
import eu.xenit.alfred.api.comments.ICommentService;
import eu.xenit.alfred.api.data.NodeRef;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentsTest extends RestV1BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CommentsTest.class);

    private static final String commentTitle = "commentTitle";
    private static final String commentContent = "Comment Content";
    private static final String COMMENTNODE1 = "testComment";
    private static final String COMMENTNODE_NORIGHTS = "testComment_noRights";
    private static final String SKIPCOUNT = "skipcount";
    private static final String PAGESIZE = "pagesize";

    private final CommentService alfrescoCommentService;
    private final ICommentService commentService;
    private final NodeService nodeService;

    public CommentsTest() {
        // initialise the local beans
        nodeService = getBean(eu.xenit.alfred.api.alfresco.metadata.NodeService.class);
        commentService = getBean(ICommentService.class);
        alfrescoCommentService = getBean("commentService", CommentService.class);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Override
    public HashMap<String, NodeRef> init() {
        HashMap<String, NodeRef> response = super.init();
        response = initComments(response);
        return response;
    }

    private HashMap<String, NodeRef> initComments(HashMap<String, NodeRef> initializedNodeRefs) {
        return this.transactionHelper.doInTransaction(() -> {
            Comment comment = commentService.addNewComment(initializedNodeRefs.get(TESTFILE_NAME), commentContent);
            initializedNodeRefs.put(COMMENTNODE1, comment.getId());
            org.alfresco.service.cmr.repository.NodeRef alfrescoCommentNode2 = alfrescoCommentService
                    .createComment(c.alfresco(initializedNodeRefs.get(NOUSERRIGHTS_FILE_NAME)), commentTitle,
                            commentContent, false);
            initializedNodeRefs.put(COMMENTNODE_NORIGHTS, new NodeRef(alfrescoCommentNode2.toString()));
            return initializedNodeRefs;
        }, false, true);
    }

    @Test
    public void testGetComments() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(RestV1BaseTest.TESTFILE_NAME), "/comments", "admin", "admin");
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT, "0");
        HttpGet req = new HttpGet(builder.build());

        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode conversation = mapper.readTree(result);
        JsonNode comments = conversation.get("comments");
        assertEquals(1, comments.size());
        assertEquals(initializedRefs.get(COMMENTNODE1).toString(), comments.get(0).get("id").textValue());
    }

    @Test
    public void testGetComment() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(COMMENTNODE1), "admin", "admin");
        HttpGet req = new HttpGet(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();

        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode comment = mapper.readTree(result);
        assertEquals(initializedRefs.get(COMMENTNODE1).toString(), comment.get("id").textValue());
    }

    @Test
    public void testGetCommentOnContent() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(TESTFILE_NAME), "admin", "admin");
        HttpGet req = new HttpGet(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();

        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testPutContent() throws IOException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(COMMENTNODE1), "admin", "admin");
        HttpPut req = new HttpPut(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        String checkoutJsonString = "{ \"content\" : \"new content\" }";
        req.setEntity(new StringEntity(checkoutJsonString, ContentType.APPLICATION_JSON));

        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode comment = mapper.readTree(result);
        assertEquals(initializedRefs.get(COMMENTNODE1).toString(), comment.get("id").textValue());
        assertEquals("new content", comment.get("content").textValue());
    }

    @Test
    public void testPutContentOnContent() throws IOException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(TESTFILE_NAME), "admin", "admin");
        HttpPut req = new HttpPut(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        String checkoutJsonString = "{ \"content\" : \"new content\" }";
        req.setEntity(new StringEntity(checkoutJsonString, ContentType.APPLICATION_JSON));

        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testDeleteComment() throws IOException, InterruptedException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeCommentsUrl(initializedRefs.get(COMMENTNODE1), "admin", "admin");

        HttpDelete req = new HttpDelete(url);
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();

        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }

        // Alfresco Cache is lagging behind...
        Thread.sleep(2000);

        boolean exists = this.transactionHelper.doInTransaction(
                () -> nodeService.exists(initializedRefs.get(COMMENTNODE1)), true, true
        );
        assertFalse(exists);
    }

    @Test
    public void testGetCommentsAccessDenied() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/comments",
                RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT, "0");
        HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetCommentsNotFound() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(new NodeRef("workspace://SpacesStore/00000000-0000-0000-0000-000000000000"),
                "/comments", "admin", "admin");
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT, "0");
        HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        assertEquals(404, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAppendComments() throws IOException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(RestV1BaseTest.TESTFILE_NAME), "/comments", "admin", "admin");

        HttpPost req = new HttpPost(url);
        String checkoutJsonString = "{ \"content\" : \"new content\" }";
        req.setEntity(new StringEntity(checkoutJsonString, ContentType.APPLICATION_JSON));
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        String result = "";
        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            result = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode comment = mapper.readTree(result);
        assertTrue(nodeService.exists(new NodeRef(comment.get("id").textValue())));
    }

}