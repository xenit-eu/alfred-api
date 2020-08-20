package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.alfresco.repo.forum.CommentService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
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
    private ApixToAlfrescoConversion apixConverter;

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
        initializedNodeRefs.put(COMMENTNODE1, new NodeRef(alfrescoCommentNode.toString()));
        org.alfresco.service.cmr.repository.NodeRef alfrescoCommentNode2 = alfrescoCommentService
                .createComment(apixConverter.alfresco(initializedNodeRefs.get(NOUSERRIGHTS_FILE_NAME)), commentTitle,
                        commentContent, false);
        initializedNodeRefs.put(COMMENTNODE_NORIGHTS, new NodeRef(alfrescoCommentNode2.toString()));
        return initializedNodeRefs;
    }

    @Test
    public void test_getComments() throws IOException, URISyntaxException {
        HashMap<String, NodeRef> initializedRefs = init();
        String url = makeNodesUrl(initializedRefs.get(BaseTest.TESTFILE_NAME), "/comments", "admin", "admin");
        log.info("URL: {}", url);
        URIBuilder builder = new URIBuilder(url);
        builder.addParameter(PAGESIZE, "10");
        builder.addParameter(SKIPCOUNT,"0");
        HttpResponse httpResponse = Request.Get(builder.build()).execute().returnResponse();
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void test_getComments_accessDenied() throws IOException, URISyntaxException {
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
    public void test_getComments_notFound() throws IOException, URISyntaxException {
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
