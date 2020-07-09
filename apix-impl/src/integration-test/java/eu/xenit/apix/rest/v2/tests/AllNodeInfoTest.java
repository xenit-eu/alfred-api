package eu.xenit.apix.rest.v2.tests;

import eu.xenit.apix.data.NodeRef;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AllNodeInfoTest extends eu.xenit.apix.rest.v2.tests.BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AllNodeInfoTest.class);

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetAllNodeInfo() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(BaseTest.TESTFILE_NAME), "admin", "admin");
        logger.info(" URL: " + url);
        for (int i = 0; i < 20; i++) {
            logger.error("For the request of testGetAllNodeInfo");
            logger.error(url);
        }

        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        logger.info(" Result: " + result);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetAllNodeInfoOfMultipleNodes() throws IOException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        NodeRef nodeRef0 = initializedNodeRefs.get(BaseTest.TESTFILE_NAME);
        NodeRef nodeRef1 = initializedNodeRefs.get(BaseTest.TESTFILE2_NAME);

        String jsonString = json(String.format(
                "{" +
                        "\"noderefs\": [\"" +
                        nodeRef0.toString() +
                        "\", \"" +
                        nodeRef1.toString() +
                        "\"" +
                        "]}"));

        logger.error("jsonString: " + jsonString);

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes/nodeInfo";
        logger.error("url: " + url);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            String responseString = EntityUtils.toString(response.getEntity());
            JSONArray responseJsonArray = new JSONArray(responseString);
            assertEquals(2, responseJsonArray.length());
        }
    }

    @Test
    public void testGetAllNodeInfoWithNoNodesListed() throws IOException {
        String jsonString = json("{}");

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes/nodeInfo";
        logger.error("url: " + url);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testGetAllNodeInfoForNodeWithoutPermissions() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url =
                makeAlfrescoBaseurl(BaseTest.USERWITHOUTRIGHTS, BaseTest.USERWITHOUTRIGHTS) + "/apix/v2/nodes/nodeInfo";
        logger.info("url: {}", url);
        String jsonString = json(
                "{" +
                        "\"noderefs\": [\"" +
                        initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME).toString() +
                        "\"" +
                        "]}");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            String responseString = EntityUtils.toString(response.getEntity());
            JSONArray responseJsonArray = new JSONArray(responseString);
            assertEquals(0, responseJsonArray.length());
        } catch (JSONException jsonException) {
            String message = "failed to deserialise responsestring";
            logger.error(message);
            fail(message);
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
