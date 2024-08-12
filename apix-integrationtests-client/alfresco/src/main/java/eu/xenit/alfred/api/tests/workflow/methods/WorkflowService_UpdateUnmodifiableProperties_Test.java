package eu.xenit.alfred.api.tests.workflow.methods;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_UpdateUnmodifiableProperties_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_UpdateUnmodifiableProperties_Test.class);

    private final HashMap<String, String> sampleUnmodifiableProperties = new HashMap<>();

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testUpdateUnmodifiableProperties", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);

        InitializeSampleProperties();
    }

    private void InitializeSampleProperties() {
        populateProperties(unmodifiableProperties, sampleUnmodifiableProperties, new HashMap<String, Serializable>());
    }

    @Test
    public void testUpdateUnmodifiableProperties() {
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

            assertTrue(apixProbeClaimTask(taskID, USER_A));
            WorkflowOrTaskChanges unmodifiablePropertiesPayload = new WorkflowOrTaskChanges(
                    sampleUnmodifiableProperties);
            assertFalse(apixProbeUpdateProperties(taskID, unmodifiablePropertiesPayload));
        }
    }
}
