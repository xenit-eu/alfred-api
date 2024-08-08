package eu.xenit.apix.tests.workflow.methods;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_ReleaseOnOtherClaimedTasks_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_ReleaseOnOtherClaimedTasks_Test.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testReleaseOnOtherClaimedTasks", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test(expected = AccessDeniedException.class)
    public void testReleaseOnOtherClaimedTasks() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOREIGNER);

        List<String> taskIDs = this.tasks.get(testName);
        for (String taskID : taskIDs) {
            if (taskID == null) {
                Assert.fail("Could not find pre-configured task for this test.");
            }

            assertFalse(apixProbeReleaseTask(taskID, USER_FOREIGNER));
            assertFalse(alfrescoProbeClaimTask(taskID, USER_FOREIGNER));
        }
    }
}
