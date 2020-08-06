package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeAssociation;
import eu.xenit.apix.node.ChildParentAssociation;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by kenneth on 17.03.16.
 */
public class CopyNodeTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(CopyNodeTest.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;


    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testCopyNode() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef parentRef = primaryParentAssoc.getTarget();

        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes";
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.TESTFILE_NAME).toString(),null, null, 200);
                    return null;
                }, false, true);

        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
        assertEquals(2, newChildAssocs.size());
    }

    @Test
    public void testCopyNodeWithProperties() throws Throwable {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        final NodeRef parentRef = primaryParentAssoc.getTarget();

        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v2/nodes";
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        final String newName = "NewName";
        final String newTitle = "NewTitle";
        String[] titleProperty = new String[]{newTitle};
        HashMap<QName, String[]> properties = new HashMap<>();
        properties.put(new QName(ContentModel.PROP_TITLE.toString()) , titleProperty);
        JSONObject response = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.TESTFILE_NAME).toString(),newName, properties, 200);
                }, false, true);

        String newRef = (String) response.get("noderef");
        JSONObject responseProperties = (JSONObject) ((JSONObject) response.get("metadata")).get("properties");
        JSONArray responseNameProperty = (JSONArray) responseProperties.get(ContentModel.PROP_NAME.toString());
        JSONArray responseTitleProperty = (JSONArray) responseProperties.get(ContentModel.PROP_TITLE.toString());
        assertEquals(true, nodeService.exists(new NodeRef(newRef)));
        assertEquals(newName, (String) responseNameProperty.get(0));
        assertEquals(newTitle, (String) responseTitleProperty.get(0));

    }

    @Test
    public void copyNodeReturnsAccesDenied() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME));
        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        final NodeRef parentRef = primaryParentAssoc.getTarget();

        final String url = makeAlfrescoBaseurl(BaseTest.USERWITHOUTRIGHTS, BaseTest.USERWITHOUTRIGHTS) + "/apix/v1/nodes";
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME).toString(), null, null, 403);
                    return null;
                }, false, true);

        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
        assertEquals(1, newChildAssocs.size());
    }

    private JSONObject doTestCopy(CloseableHttpClient httpClient, String url, NodeRef parentRef, String copyFrom, String name, HashMap<QName, String[]> properties, int expectedResponseCode) throws Throwable {
        HttpPost httppost = new HttpPost(url);
        String jsonBody = "{";
        if ( parentRef != null ) {
            jsonBody += "\"parent\":\"" + parentRef +  "\",";
        }
        if ( copyFrom != null ) {
            jsonBody += "\"copyFrom\":\"" + copyFrom +  "\",";
        }
        if ( name != null ) {
            jsonBody += "\"name\":\"" + name +  "\",";
        }
        if ( properties != null ) {
            jsonBody += "\"properties\":{";
            for (Map.Entry<QName, String[]> entry : properties.entrySet()){
                jsonBody += "\""+ entry.getKey().toString() +"\":[";
                    for (String value : entry.getValue()) {
                        jsonBody += "\"" + value + "\",";
                    }
                    //Cut off last comma
                    jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
                    jsonBody +=  "],";
            }
            //Cut off last comma
            jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
            jsonBody += "}";
        }
        //Cut off last comma
        System.out.println("Json body : " + jsonBody);
        if (properties == null) {
            jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
        }
        jsonBody += "}";
        String jsonString = json(jsonBody);
        httppost.setEntity(new StringEntity(jsonString));

        String nodeInfo;
        try (CloseableHttpResponse response = httpClient.execute(httppost)) {
            nodeInfo = EntityUtils.toString(response.getEntity());
            assertEquals(expectedResponseCode, response.getStatusLine().getStatusCode());
        }

        if (expectedResponseCode == 200) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(nodeInfo);
            return json;
        }
        return null;
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
