package eu.xenit.alfred.api.tests.workflow.methods;

import static org.junit.Assert.assertEquals;

import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.search.Sorting;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.alfred.api.workflow.search.TaskSearchQuery;
import java.util.ArrayList;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_SearchContextAllTasks_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowService_SearchContextAllTasks_Test.class);

    protected void setupSampleDataLocal() {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);

        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testSearchContextAllTasksAsAdmin", new ArrayList<Map<String, WorkflowPath>>());
                        // Task 1
                        tasksToMap.get("testSearchContextAllTasksAsAdmin")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_REVIEW, USER_A, GROUP_USERS));
                        // Task 2
                        tasksToMap.get("testSearchContextAllTasksAsAdmin")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        // Task 3
                        tasksToMap.get("testSearchContextAllTasksAsAdmin")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        logger.error(
                                "WorkflowService_SearchContextAllTasks_Test.setupSampleDataLocal(): tasksToMap.get(\"testSearchContextAllTasksAsAdmin\").size(): "
                                        + tasksToMap.get("testSearchContextAllTasksAsAdmin").size());
                        return null;
                    }
                }, false, true);

        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testSearchContextAllTasksAsUser", new ArrayList<Map<String, WorkflowPath>>());
                        // Task 4
                        tasksToMap.get("testSearchContextAllTasksAsUser")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_REVIEW, USER_B, GROUP_USERS));
                        // Task 5
                        tasksToMap.get("testSearchContextAllTasksAsUser")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_B, GROUP_USERS));
                        // Task 6
                        tasksToMap.get("testSearchContextAllTasksAsUser")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_B, GROUP_USERS));
                        logger.error(
                                "WorkflowService_SearchContextAllTasks_Test.setupSampleDataLocal(): tasksToMap.get(\"testSearchContextAllTasksAsUser\").size(): "
                                        + tasksToMap.get("testSearchContextAllTasksAsUser").size());
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testSearchContextAllTasksAsUser() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        TaskSearchQuery searchQuery = createNewSearchQuery(TaskSearchQuery.QueryScope.AllTasks, 0, 15,
                Sorting.ASCENDING, IWorkflowService.ALFRESCO_STARTDATE);

        TaskOrWorkflowSearchResult result = getSearchResultTasks(searchQuery);
        assertEquals(4, result.results.size());
        // Tasks 1,4,5,6
    }

    @Test
    public void testSearchContextAllTasksAsAdmin() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);

        TaskSearchQuery searchQuery = createNewSearchQuery(TaskSearchQuery.QueryScope.AllTasks, 0, 15,
                Sorting.ASCENDING, IWorkflowService.ALFRESCO_STARTDATE);

        TaskOrWorkflowSearchResult result = getSearchResultTasks(searchQuery);
        assertEquals(3, result.results.size());
        // Tasks 1,2,3
    }
}
