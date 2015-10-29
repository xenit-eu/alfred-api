package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
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

/**
 * Created by kenneth on 16.03.16.
 */
public class AllNodeInfoTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AllNodeInfoTest.class);

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetAllNodeInfo() throws IOException {
        NodeRef[] nodeRef = init();
        String url = makeNodesUrl(nodeRef[0], "admin", "admin");
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
        NodeRef[] nodeRefs = init();
        NodeRef nodeRef0 = nodeRefs[0];
        NodeRef nodeRef1 = nodeRefs[1];

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
        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/nodes/nodeInfo";
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

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
