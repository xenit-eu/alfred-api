package eu.xenit.alfred.api.tests.workflow.methods;

import static org.junit.Assert.assertFalse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_GetWorkflowByIDIDontHaveAccessTo_Test extends
        WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_GetWorkflowByIDIDontHaveAccessTo_Test.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        wfPaths.put("testGetWorkflowByIDIDontHaveAccessTo",
                                createWorkflow(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testGetWorkflowByIDIDontHaveAccessTo() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOREIGNER);
        WorkflowPath workflow = this.wfPaths.get(testName);

        if (workflow == null) {
            Assert.fail("Could not find pre-configured workflow for this test.");
        }
        assertFalse(hasAccessToWorkflowInstance(workflow));
    }
}
