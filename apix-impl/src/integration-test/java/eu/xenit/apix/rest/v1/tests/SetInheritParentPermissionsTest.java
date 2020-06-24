package eu.xenit.apix.rest.v1.tests;

import static junit.framework.TestCase.assertTrue;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v2.tests.AllNodeInfoTest;
import eu.xenit.apix.rest.v2.tests.BaseTest;
import java.io.IOException;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class SetInheritParentPermissionsTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(AllNodeInfoTest.class);
    @Autowired
    IPermissionService permissionService;
    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testSetInheritPermissionsRestTrue() throws IOException, InterruptedException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        NodeRef nodeRef0 = initializedNodeRefs.get(BaseTest.TESTFILE_NAME);
        setInheritUrl(nodeRef0, true);
        assertInheritUrl(nodeRef0, true);
    }

    @Test
    public void testSetInheritPermissionsRestFalse() throws IOException, InterruptedException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        NodeRef nodeRef0 = initializedNodeRefs.get(BaseTest.TESTFILE_NAME);
        setInheritUrl(nodeRef0, false);
        assertInheritUrl(nodeRef0, false);
    }

    private void setInheritUrl(final NodeRef nodeRef0, final boolean inherit) throws IOException, JSONException {
        String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/nodes/" + nodeRef0.GetApixUrl()
                + "/acl/inheritFromParent";
        HttpPost httppost = new HttpPost(url);
        String body = "{\"inheritFromParent\":\"" + inherit + "\"}";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            httppost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(httppost);
        }
    }

    private void assertInheritUrl(final NodeRef nodeRef0, boolean value) throws IOException, JSONException {
        String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/nodes/" + nodeRef0.GetApixUrl() + "/acl";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpclient.execute(new HttpGet(url));
            String jsonResp = EntityUtils.toString(response.getEntity());
            String contains = "\"inheritFromParent\":" + value;
            assertTrue(jsonResp.indexOf(contains) >= 0);
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
