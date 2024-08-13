package eu.xenit.alfred.api.tests.workflow.methods;

import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
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

public class WorkflowService_UpdateMixedProperties_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_UpdateMixedProperties_Test.class);

    private final Map<String, String> sampleMixedProperties = new HashMap<>();
    private final Map<String, Serializable> sampleMixedModifiableProperties = new HashMap<>();
    private final Map<String, Serializable> sampleMixedUnmodifiableProperties = new HashMap<>();

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testUpdateMixedProperties", new ArrayList() {{
                            add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        }});
                        return null;
                    }
                }, false, true);

        InitializeSampleProperties();
    }

    private void InitializeSampleProperties() {
        populateProperties(modifiableProperties, sampleMixedProperties, sampleMixedModifiableProperties);
        populateProperties(unmodifiableProperties, sampleMixedProperties, sampleMixedUnmodifiableProperties);
    }

    @Test
    public void testUpdateMixedProperties() {
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
            WorkflowOrTaskChanges mixedPropertiesPayload = new WorkflowOrTaskChanges(sampleMixedProperties);
            assertTrue(alfredApiProbeUpdateProperties(taskID, mixedPropertiesPayload, unmodifiableProperties));
        }
    }
}
