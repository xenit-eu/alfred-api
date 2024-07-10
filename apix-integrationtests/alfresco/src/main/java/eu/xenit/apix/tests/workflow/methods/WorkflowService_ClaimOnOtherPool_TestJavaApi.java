package eu.xenit.apix.tests.workflow.methods;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_ClaimOnOtherPool_TestJavaApi extends WorkflowService_JavaApi_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_ClaimOnOtherPool_TestJavaApi.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testClaimOnOtherPool", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test(expected = AccessDeniedException.class)
    public void testClaimOnOtherPool() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOREIGNER);
        String currentUserName = this.authenticationService.getCurrentUserName();

        List<String> taskIDs = this.tasks.get(testName);
        for (String taskID : taskIDs) {
            if (taskID == null) {
                Assert.fail("Could not find pre-configured task for this test.");
            }

            try {
                setOwner(taskID, currentUserName);
                Assert.fail("User can claim workflow task he is not supposed to be able to claim");
            } catch (Exception e) {
                // Expected exception, swallow and proceed with the test
            }

            this.apixWorkflowService.claimWorkflowTask(taskID, USER_FOREIGNER);
        }
    }
}
