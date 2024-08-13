package eu.xenit.alfred.api.tests.workflow.methods;

import eu.xenit.alfred.api.tests.workflow.WorkflowServiceBaseTest;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.util.ISO8601DateFormat;
import org.junit.After;
import org.junit.Before;

public abstract class WorkflowService_BaseMethod_Test extends WorkflowServiceBaseTest {

    protected final Map<String, List<String>> tasks = new HashMap<>();
    protected final Map<String, ArrayList<Map<String, WorkflowPath>>> tasksToMap = new HashMap<>();

    protected final Map<String, String> unmodifiableProperties = new HashMap<String, String>() {{
        put(IWorkflowService.ALFRESCO_CONTEXT, null);
        put(IWorkflowService.ALFRESCO_DESCRIPTION, Long.toString(new Random().nextLong()));
        put(IWorkflowService.ALFRESCO_ACTIVE, Boolean.TRUE.toString());
        put(IWorkflowService.ALFRESCO_CLAIMABLE, Boolean.TRUE.toString());
        put(IWorkflowService.ALFRESCO_RELEASABLE, Boolean.FALSE.toString());
        put(IWorkflowService.ALFRESCO_TITLE, Long.toString(new Random().nextLong()));
    }};

    protected final Map<String, String> modifiableProperties = new HashMap<String, String>() {{
        put(IWorkflowService.ALFRESCO_DUEDATE, ISO8601DateFormat.format(new Date()));
        put(IWorkflowService.ALFRESCO_PRIORITY, "1");
        put(WorkflowModel.PROP_STATUS.toString(), "In Progress");
    }};

    protected abstract void setupSampleDataLocal();

    @Before
    public void SetupLocal() {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        super.SetupLocal();
        this.cleanUpAllWorkflows();
        this.createTestUsersAndGroups();
        this.setupSampleData();
    }

    private void setupSampleData() {
        this.setupSampleDataLocal();
        logger.debug("Created '" + tasksToMap.size() + "' tasks");
        extractTasksFromWorkflows(tasksToMap, tasks);
    }

    private void extractTasksFromWorkflows(Map<String, ArrayList<Map<String, WorkflowPath>>> sourceWorkflows,
            Map<String, List<String>> targetTasksMap) {
        final Map<String, ArrayList<Map<String, WorkflowPath>>> localTasksToMap = sourceWorkflows;
        final Map<String, List<String>> finalTargetTasksMap = targetTasksMap;

        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        for (String methodName : localTasksToMap.keySet()) {
                            logger.debug("Mapping task to method '" + methodName + "'");
                            ArrayList<Map<String, WorkflowPath>> userToWorkflowMap = localTasksToMap.get(methodName);
                            for (Map<String, WorkflowPath> userWorkflows : userToWorkflowMap) {
                                for (String user : userWorkflows.keySet()) {
                                    logger.debug("Mapping workflow path to user '" + user + "'");
                                    List<String> taskIds = getTask(userWorkflows.get(user), user);
                                    logger.debug("Found " + taskIds.size() + " tasks for user '" + user + "'");
                                    for (String taskId : taskIds) {
                                        logger.debug("Found task ID '" + taskId + "' for user '" + user + "'");
                                        if (!finalTargetTasksMap.containsKey(methodName)) {
                                            finalTargetTasksMap.put(methodName, new ArrayList<String>());
                                        }
                                        if (!finalTargetTasksMap.get(methodName).contains(taskId)) {
                                            finalTargetTasksMap.get(methodName).add(taskId);
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }, false, true);
    }

    protected void populateProperties(Map<String, String> sourceProperties,
            Map<String, String> allPropertiesToBeUpdated, Map<String, Serializable> isolatedPropertiesToBeCompared) {
        logger.debug("populateProperties: input sourceProperties.size() = " + sourceProperties.size());

        for (String property : sourceProperties.keySet()) {
            String value = sourceProperties.get(property);
            allPropertiesToBeUpdated.put(property, value);
            isolatedPropertiesToBeCompared.put(property, value);
        }

        logger.debug("populateProperties: output allPropertiesToBeUpdated.size() = " + allPropertiesToBeUpdated.size());
        logger.debug(
                "populateProperties: output isolatedPropertiesToBeCompared.size() = " + isolatedPropertiesToBeCompared
                        .size());
    }

    @After
    public void cleanUpLocal() {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        super.cleanUpLocal();
    }
}
