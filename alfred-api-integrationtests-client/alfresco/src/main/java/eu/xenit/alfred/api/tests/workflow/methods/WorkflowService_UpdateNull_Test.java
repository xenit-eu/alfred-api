package eu.xenit.alfred.api.tests.workflow.methods;

import static org.junit.Assert.assertFalse;

import eu.xenit.alfred.api.workflow.model.Task;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_UpdateNull_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_UpdateNull_Test.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testUpdateNull", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testUpdateNull() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        List<String> taskIDs = this.tasks.get(testName);
        for (String taskID : taskIDs) {
            if (taskID == null) {
                Assert.fail("Could not find pre-configured task for this test.");
            }

            Map<String, Serializable> oldProperties = this.alfredApiWorkflowService.getTaskInfo(taskID).getProperties();
            Task updatedTask = this.alfredApiWorkflowService.updateTask(taskID, null);
            assertFalse(noPropertiesApplied(oldProperties, updatedTask.getProperties()));
        }
    }
}
