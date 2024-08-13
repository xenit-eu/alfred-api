package eu.xenit.alfred.api.tests.workflow.methods;

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

public class WorkflowService_UpdateModifiableProperties_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_UpdateModifiableProperties_Test.class);

    private final HashMap<String, String> sampleModifiableProperties = new HashMap<>();

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testUpdateModifiableProperties", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);

        InitializeSampleProperties();
    }

    private void InitializeSampleProperties() {
        populateProperties(modifiableProperties, sampleModifiableProperties, new HashMap<String, Serializable>());
    }

    @Test
    public void testUpdateModifiableProperties() {
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

            assertTrue(alfredApiProbeClaimTask(taskID, USER_A));
            WorkflowOrTaskChanges modifiablePropertiesPayload = new WorkflowOrTaskChanges(sampleModifiableProperties);
            assertTrue(alfredApiProbeUpdateProperties(taskID, modifiablePropertiesPayload));
        }
    }
}
