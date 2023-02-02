package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.data.NodeRef;
import java.io.IOException;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by kenneth on 16.03.16.
 */
public class PathTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(PathTest.class);

    @Autowired
    @Qualifier("NodeService")
    NodeService nodeService;

    @Autowired
    @Qualifier("PermissionService")
    PermissionService permissionService;

    @Autowired
    @Qualifier("NamespaceService")
    NamespaceService namespaceService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testDisplayPathGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/path", "admin", "admin");

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(httpResponse.getEntity());
        logger.debug(" DisplayPath :" + result);

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        String expectedResult = String.format("\"displayPath\":\"%s\"",
                this.nodeService.getPath(new org.alfresco.service.cmr.repository.NodeRef(initializedNodeRefs.get(
                        RestV1BaseTest.TESTFILE_NAME).getValue()))
                        .toDisplayPath(this.nodeService, this.permissionService));
        assertTrue(result.contains(expectedResult));
    }

    @Test
    public void testDisplayPathGetReturnsAccesDenied() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/path", RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testQNamePathGet() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/path", "admin", "admin");

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(httpResponse.getEntity());
        logger.debug(" QNamePath :" + result);

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        String expectedResult = String.format("\"qnamePath\":\"%s\"",
                this.nodeService.getPath(new org.alfresco.service.cmr.repository.NodeRef(initializedNodeRefs.get(
                        RestV1BaseTest.TESTFILE_NAME).getValue()))
                        .toPrefixString(namespaceService));
        assertTrue(result.contains(expectedResult));
    }

    @Test
    public void testQNamePathGetReturnsAccesDenied() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/path", RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        assertEquals(403, httpResponse.getStatusLine().getStatusCode());
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
