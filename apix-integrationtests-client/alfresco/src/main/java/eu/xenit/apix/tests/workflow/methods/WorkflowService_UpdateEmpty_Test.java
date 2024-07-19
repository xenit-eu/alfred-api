package eu.xenit.apix.tests.workflow.methods;

import static org.junit.Assert.assertFalse;

import eu.xenit.apix.workflow.model.Task;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_UpdateEmpty_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_UpdateEmpty_Test.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testUpdateEmpty", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testUpdateEmpty() {
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

            WorkflowOrTaskChanges emptyPropertiesPayload = new WorkflowOrTaskChanges(new HashMap<String, String>());
            Map<String, Serializable> oldProperties = this.apixWorkflowService.getTaskInfo(taskID).getProperties();
            Task updatedTask = this.apixWorkflowService.updateTask(taskID, emptyPropertiesPayload);
            assertFalse(noPropertiesApplied(oldProperties, updatedTask.getProperties()));
        }
    }
}
