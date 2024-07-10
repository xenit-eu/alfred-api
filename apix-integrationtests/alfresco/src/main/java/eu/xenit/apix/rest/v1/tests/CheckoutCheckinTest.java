package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.workingcopies.NoderefResult;
import java.io.IOException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by kenneth on 31.03.16.
 */
public class CheckoutCheckinTest extends RestV1BaseTest {
    private NodeService nodeService;
    private NodeRef originalNoderef;

    public CheckoutCheckinTest(){
        // initialise the local beans
        nodeService = serviceRegistry.getNodeService();
        originalNoderef = init().get(RestV1BaseTest.TESTFILE_NAME);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
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
