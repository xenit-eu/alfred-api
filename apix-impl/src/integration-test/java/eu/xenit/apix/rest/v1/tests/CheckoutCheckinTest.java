package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.workingcopies.NoderefResult;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kenneth on 31.03.16.
 */
public class CheckoutCheckinTest extends BaseTest {

    @Autowired
    NodeService nodeService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    ApixToAlfrescoConversion c;
    private NodeRef originalNoderef;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        originalNoderef = init().get(BaseTest.TESTFILE_NAME);
    }

    @Test
    public void testCheckoutPostAndCheckin() throws IOException, JSONException {
        NoderefResult checkoutResult = doPost(makeAlfrescoBaseurlAdmin() + "/apix/v1/workingcopies",
                NoderefResult.class,
                "{'original':'%s'}", originalNoderef.getValue());

        NodeRef workingCopyRef = checkoutResult.getNoderef();

        final NodeRef testWorkingCopyRef = workingCopyRef;
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        assertTrue(nodeService.hasAspect(
                                new org.alfresco.service.cmr.repository.NodeRef(testWorkingCopyRef.toString()),
                                ContentModel.ASPECT_WORKING_COPY));
                        return null;
                    }
                }, false, true);

        String checkinUrl = this.makeWorkingCopiesUrl(workingCopyRef.getStoreRefProtocol(),
                workingCopyRef.getStoreRefId(),
                workingCopyRef.getGuid(),
                "/checkin",
                "admin",
                "admin");

        final NoderefResult result = doPost(checkinUrl, NoderefResult.class, "{'comment':'','majorVersion':false}");

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        assertEquals(originalNoderef, result.getNoderef());
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testCancelCheckout() throws IOException, JSONException {

        NoderefResult checkoutResult = doPost(makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/workingcopies",
                NoderefResult.class,
                "{'original':'%s'}"
                , originalNoderef.getValue());

        final NodeRef workingCopyRef = checkoutResult.getNoderef();

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        assertTrue(nodeService.hasAspect(c.alfresco(workingCopyRef), ContentModel.ASPECT_WORKING_COPY));
                        return null;
                    }
                });

        NodeRef ref = doDelete(makeWorkingCopiesUrl(workingCopyRef, "", "admin", "admin"), NoderefResult.class)
                .getNoderef();
        assertEquals(ref, originalNoderef);
    }

    @Test
    public void testGetWorkingCopySource() throws IOException, JSONException {

        NoderefResult checkoutResult = doPost(makeAlfrescoBaseurlAdmin() + "/apix/v1/workingcopies",
                NoderefResult.class,
                "{'original':'%s'}", originalNoderef.getValue());

        NodeRef workingCopyRef = checkoutResult.getNoderef();

        final NoderefResult result = doGet(makeWorkingCopiesUrl(workingCopyRef, "/original", "admin", "admin"),
                NoderefResult.class);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        assertEquals(result.getNoderef().toString(), originalNoderef.toString());

                        return null;
                    }
                }, false, true);
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
