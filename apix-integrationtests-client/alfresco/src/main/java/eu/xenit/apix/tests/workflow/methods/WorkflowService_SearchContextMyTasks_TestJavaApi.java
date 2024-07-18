package eu.xenit.apix.tests.workflow.methods;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.search.Sorting;
import eu.xenit.apix.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.apix.workflow.search.TaskSearchQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_SearchContextMyTasks_TestJavaApi extends WorkflowService_JavaApi_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_SearchContextMyTasks_TestJavaApi.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testSearchContextMyTask", new ArrayList<Map<String, WorkflowPath>>());
                        tasksToMap.get("testSearchContextMyTask")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_REVIEW, USER_A, GROUP_USERS));
                        tasksToMap.get("testSearchContextMyTask")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        tasksToMap.get("testSearchContextMyTask")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testSearchContextMyTask() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        List<String> taskIds = this.tasks.get(testName);
        logger.debug("testSearchContextMyTask: taskIds.size(): " + taskIds.size());

        apixProbeClaimTask(taskIds.get(1), USER_A);
        TaskSearchQuery searchQuery = createNewSearchQuery(TaskSearchQuery.QueryScope.MyTasks, 0, 5, Sorting.ASCENDING,
                IWorkflowService.ALFRESCO_STARTDATE);

        TaskOrWorkflowSearchResult result = getSearchResultTasks(searchQuery);
        assertEquals(2, result.results.size());
    }
}
