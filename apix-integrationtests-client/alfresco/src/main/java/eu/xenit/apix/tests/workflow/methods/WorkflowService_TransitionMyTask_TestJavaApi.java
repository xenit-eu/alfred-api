package eu.xenit.apix.tests.workflow.methods;

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

public class WorkflowService_TransitionMyTask_TestJavaApi extends WorkflowService_JavaApi_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_TransitionMyTask_TestJavaApi.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testTransitioningMyTasks", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testTransitioningMyTasks() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        List<String> taskIDs = this.tasks.get(testName);
        for (String taskId : taskIDs) {
            if (taskId == null) {
                Assert.fail("Could not find pre-configured task for this test.");
            }

            Map<String, List<String>> targetTasksMap = new HashMap<>();
            initTransitionsAndTasks(testName, taskId, targetTasksMap);

            for (String transition : targetTasksMap.keySet()) {
                List<String> taskIdToTransitions = targetTasksMap.get(transition);
                for (String taskIdToTransition : taskIdToTransitions) {
                    logger.debug("Claimed task: '" + taskIdToTransition + "' " + apixProbeClaimTask(taskIdToTransition,
                            USER_A));
                    probeApixTransitionTask(transition, taskIdToTransition);
                }
            }
        }
    }
}
