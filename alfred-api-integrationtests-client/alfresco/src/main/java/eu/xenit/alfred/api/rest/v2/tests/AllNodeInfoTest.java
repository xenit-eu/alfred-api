package eu.xenit.alfred.api.rest.v2.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.v1.tests.RestV1BaseTest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
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

public class AllNodeInfoTest extends RestV2BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AllNodeInfoTest.class);

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetAllNodeInfo() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV2BaseTest.TESTFILE_NAME), "admin", "admin");
        logger.debug(" URL: " + url);
        HttpResponse response = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());
        logger.debug(" Result: " + result);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetAllNodeInfoOfMultipleNodes() throws IOException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        NodeRef nodeRef0 = initializedNodeRefs.get(RestV2BaseTest.TESTFILE_NAME);
        NodeRef nodeRef1 = initializedNodeRefs.get(RestV2BaseTest.TESTFILE2_NAME);

        String jsonString = json(String.format(
                "{" +
                        "\"noderefs\": [\"" +
                        nodeRef0.toString() +
                        "\", \"" +
                        nodeRef1.toString() +
                        "\"" +
                        "]}"));

        logger.debug("jsonString: " + jsonString);

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes/nodeInfo";
        logger.debug("url: " + url);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            String responseString = EntityUtils.toString(response.getEntity());
            JSONArray responseJsonArray = new JSONArray(responseString);
            assertEquals(2, responseJsonArray.length());
        }
    }

    @Test
    public void testGetAllNodeInfoWithNoNodesListed() throws IOException {
        String jsonString = json("");

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes/nodeInfo";
        logger.debug("url: " + url);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testGetAllNodeInfoForNodeWithoutPermissions() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url =
                makeAlfrescoBaseurl(RestV2BaseTest.USERWITHOUTRIGHTS, RestV2BaseTest.USERWITHOUTRIGHTS)
                        + "/apix/v2/nodes/nodeInfo";
        logger.debug("url: {}", url);
        String jsonString = json(
                "{" +
                        "\"noderefs\": [\"" +
                        initializedNodeRefs.get(RestV2BaseTest.NOUSERRIGHTS_FILE_NAME).toString() +
                        "\"" +
                        "]}");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

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

    @Test
    public void testGetAllNodeInfoWithoutNodeWithoutPermissions() throws IOException, JSONException {
        Map<String, NodeRef> initializedNodes = init();
        String jsonString = json("{\"noderefs\":["
                + "\"" + initializedNodes.get(RestV1BaseTest.TESTFILE_NAME).toString() + "\"," //regular node
                + "\"workspace://SpacesStore/12345678-1234-1234-1234-123456789012\"," //non-existing node
                + "\"" + initializedNodes.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME).toString() + "\""
                //no-permissions node
                + "]}");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final String url = makeAlfrescoBaseurl(
                RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS) + "/apix/v1/nodes/nodeInfo";
        logger.debug("url: " + url);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            String responseString = EntityUtils.toString(response.getEntity());
            JSONArray responseJsonArray = new JSONArray(responseString);
            assertEquals(1, responseJsonArray.length());
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
