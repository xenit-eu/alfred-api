package eu.xenit.apix.tests.workflow.methods;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.search.Sorting;
import eu.xenit.apix.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.apix.workflow.search.TaskSearchQuery;
import java.util.ArrayList;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService_SearchContextMyPooledTasks_Test extends WorkflowService_BaseMethod_Test {

    protected final static Logger logger = LoggerFactory
            .getLogger(WorkflowService_SearchContextMyPooledTasks_Test.class);

    protected void setupSampleDataLocal() {
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        tasksToMap.put("testSearchContextMyPooledTask", new ArrayList<Map<String, WorkflowPath>>());
                        tasksToMap.get("testSearchContextMyPooledTask")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        tasksToMap.get("testSearchContextMyPooledTask")
                                .add(createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A, GROUP_USERS));
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testSearchContextMyPooledTask() {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        logger.debug(" ===================================  TEST ==================================== ");
        logger.debug(" " + testName);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_A);

        TaskSearchQuery searchQuery = createNewSearchQuery(TaskSearchQuery.QueryScope.MyPooledTasks, 0, 5,
                Sorting.ASCENDING, IWorkflowService.ALFRESCO_STARTDATE);

        TaskOrWorkflowSearchResult result = getSearchResultTasks(searchQuery);
        assertEquals(2, result.results.size());
    }
}
