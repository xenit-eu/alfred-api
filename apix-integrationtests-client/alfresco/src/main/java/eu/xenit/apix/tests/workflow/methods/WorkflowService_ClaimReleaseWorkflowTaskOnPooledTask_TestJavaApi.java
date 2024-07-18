package eu.xenit.apix.tests.workflow.methods;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_ClaimReleaseWorkflowTaskOnPooledTask_TestJavaApi extends
        WorkflowService_JavaApi_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_ClaimReleaseWorkflowTaskOnPooledTask_TestJavaApi.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testClaimReleaseWorkflowTaskOnPooledTask", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testClaimReleaseWorkflowTaskOnPooledTask() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);
        String currentUserName = authenticationService.getCurrentUserName();
        logger.debug("User: " + currentUserName);

        String wfTask = null;
        List<WorkflowTask> pooledTasks = getPooledTasks(currentUserName);
        List<String> taskIDs = this.tasks.get(testName);
        for (String registeredWfTask : taskIDs) {
            for (WorkflowTask task : pooledTasks) {
                if (task.getId().equals(registeredWfTask)) {
                    wfTask = task.getId();
                    break;
                }
            }

            if (wfTask == null) {
                Assert.fail("Could not find pre-configured task for this test.");
            }

            final String finalWfTask = wfTask;
            assertTrue(probeClaimAndRelease(finalWfTask, currentUserName));
        }
    }
}
