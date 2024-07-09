package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.workingcopies.NoderefResult;
import eu.xenit.apix.server.ApplicationContextProvider;
import java.io.IOException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Created by kenneth on 31.03.16.
 */
public class CheckoutCheckinTest extends RestV1BaseTest {
    private ApplicationContext testApplicationContext;

    NodeService nodeService;

    TransactionService transactionService;
    ApixToAlfrescoConversion c;
    private NodeRef originalNoderef;
    ServiceRegistry serviceRegistry;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Setup the RestV1BaseTest Beans
        initialiseBeans();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        serviceRegistry = (ServiceRegistry) testApplicationContext.getBean(ServiceRegistry.class);
        c =  (ApixToAlfrescoConversion) testApplicationContext.getBean(ApixToAlfrescoConversion.class);
        transactionService = (TransactionService) testApplicationContext.getBean(TransactionService.class);
        nodeService = serviceRegistry.getNodeService();
        originalNoderef = init().get(RestV1BaseTest.TESTFILE_NAME);
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
