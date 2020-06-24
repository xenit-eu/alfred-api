package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeAssociation;
import eu.xenit.apix.node.ChildParentAssociation;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

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

        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/nodes";
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.TESTFILE_NAME).toString(), 200);
                    return null;
                }, false, true);

        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
        assertEquals(2, newChildAssocs.size());
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
                    doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME).toString(), 403);
                    return null;
                }, false, true);

        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
        assertEquals(1, newChildAssocs.size());
    }

    private void doTestCopy(CloseableHttpClient httpClient, String url, NodeRef parentRef, String targetRef, int expectedResponseCode) throws Throwable {
        HttpPost httppost = new HttpPost(url);
        String jsonString = json(String.format(
                "{" +
                        "\"parent\":\"%s\"," +
                        "\"copyFrom\":\"%s\"" +
                        "}"
                , parentRef.toString(), targetRef));
        httppost.setEntity(new StringEntity(jsonString));
        List<ChildParentAssociation> childAssociations = nodeService.getChildAssociations(parentRef);
        assertEquals(1, childAssociations.size());

        try (CloseableHttpResponse response = httpClient.execute(httppost)) {
            logger.debug(EntityUtils.toString(response.getEntity()));
            assertEquals(expectedResponseCode, response.getStatusLine().getStatusCode());
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
