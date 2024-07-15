package eu.xenit.apix.tests.workflow;

import eu.xenit.apix.tests.JavaApiBaseTest;
import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.model.Task;
import eu.xenit.apix.workflow.model.Workflow;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.apix.workflow.search.Paging;
import eu.xenit.apix.workflow.search.Sorting;
import eu.xenit.apix.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.apix.workflow.search.TaskSearchQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class WorkflowServiceJavaApiBaseTest extends JavaApiBaseTest {

    protected final static Logger logger = LoggerFactory.getLogger(WorkflowServiceJavaApiBaseTest.class);
    protected static final String USER_ADMIN = "admin";
    protected static final String USER_A = "A";
    protected static final String USER_B = "B";
    protected static final String USER_FOREIGNER = "FOREIGNER";
    protected static final String GROUP_ADMIN = "GROUP_ALFRESCO_ADMINISTRATORS";
    protected static final String GROUP_ADMIN_shortName = "ALFRESCO_ADMINISTRATORS";
    protected static final String GROUP_USERS = "GROUP_USERS";
    protected static final String ACTIVITI_REVIEW = "activiti$activitiReview";
    protected static final String ACTIVITI_POOLED_REVIEW = "activiti$activitiReviewPooled";
    private static final String GROUP_USERS_shortName = "USERS";
    private static final String GROUP_FOREIGNERS = "GROUP_FOREIGNERS";
    private static final String GROUP_FOREIGNERS_shortName = "FOREIGNERS";
    protected final Map<String, WorkflowPath> wfPaths = new HashMap<>();
    protected final Map<String, Map<String, String>> taskTransitionRegistry = new HashMap<>();
    protected final Map<String, String> tasks = new HashMap<>();
    private final List<FileInfo> tempFiles = new ArrayList<>();

    protected IWorkflowService apixWorkflowService;

    protected MutableAuthenticationService authenticationService;
    protected NodeRef companyHomeNodeRef = null;
    private PersonService personService;
    private WorkflowService workflowService;
    private AuthorityService authorityService;

    @Before
    public void initialiseBeansWorkflowServiceBaseTest() {
        apixWorkflowService = testApplicationContext.getBean("eu.xenit.apix.workflow.IWorkflowService",IWorkflowService.class);
    }

    protected void SetupLocal() {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        companyHomeNodeRef = getNodeAtPath("/app:company_home");
        setupServices();
        printWorkflowDefinitions();
    }

    //<editor-fold desc="Helper methods">

    protected Map<String, WorkflowPath> createSampleWorkflowUserMethodMap(String wfDefinition, String username,
            String group) {
        Map<String, WorkflowPath> result = new HashMap<>();
        WorkflowPath testClaimOnReviewTask = createWorkflow(wfDefinition, username, group);
        if (testClaimOnReviewTask != null) {
            result.put(username, testClaimOnReviewTask);
        } else {
            logger.debug("Workflow for testClaimOnReviewTask of type '" + wfDefinition + "' is null");
        }
        return result;
    }

    protected List<String> getTask(WorkflowPath workflowPath, String user) {
        String newWfInstanceID = workflowPath.getInstance().getId();

        List<WorkflowTask> pooledTasks = this.workflowService.getPooledTasks(user);
        List<WorkflowTask> wfTasks = this.workflowService.getTasksForWorkflowPath(newWfInstanceID);
        List<WorkflowTask> allTasks = new ArrayList<>();

        allTasks.add(this.workflowService.getStartTask(newWfInstanceID));

        logger.debug("getTask: pooledTasks: " + pooledTasks.size());
        logger.debug("getTask: wfTasks: " + wfTasks.size());

        for (WorkflowTask task : pooledTasks) {
            boolean exists = false;
            for (WorkflowTask existentTask : allTasks) {
                if (existentTask.getId().equals(task.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allTasks.add(task);
            }
        }

        for (WorkflowTask task : wfTasks) {
            boolean exists = false;
            for (WorkflowTask existentTask : allTasks) {
                if (existentTask.getId().equals(task.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allTasks.add(task);
            }
        }

        logger.debug("getTask: allTasks: " + allTasks.size());

        if (allTasks.isEmpty()) {
            Assert.fail("No tasks available for workflow ID: '"
                    + newWfInstanceID + "' or user '" + user + "'");
        }

        List<String> results = new ArrayList<>();
        WorkflowTask lastStartTask = null;

        // Find task different than start task
        for (WorkflowTask task : allTasks) {
            String taskID = task.getId().toLowerCase();
            String wfInstanceID = task.getPath().getInstance().getId();
            if (wfInstanceID.equals(newWfInstanceID)) {
                logger.debug("Match for WfInstanceID: '" + newWfInstanceID + "' ; wfTaskId: '" + taskID + "'");
                if (!taskID.contains("start")) {
                    results.add(task.getId());
                } else {
                    lastStartTask = task;
                }
            }
        }

        if (!results.isEmpty()) {
            return results;
        }

        if (lastStartTask != null) {
            logger.debug(
                    "Returning the first task with ID: " + lastStartTask.getId() + " for workflow ID: " + lastStartTask
                            .getPath().getInstance().getId());
            results.add(lastStartTask.getId());
            return results;
        }

        logger.debug("No tasks for workflow " + newWfInstanceID + " found!");
        return null;
    }

    protected void printWorkflowDefinitions() {
        if (logger.isDebugEnabled()) {
            logger.debug("Listing workflow definitions...");
            for (WorkflowDefinition workflowDef : this.workflowService.getAllDefinitions()) {
                logger.debug("DEFINITION"
                        + System.lineSeparator() + "description: " + workflowDef.getDescription()
                        + System.lineSeparator() + "id: " + workflowDef.getId()
                        + System.lineSeparator() + "name: " + workflowDef.getName()
                        + System.lineSeparator() + "title: " + workflowDef.getTitle()
                        + System.lineSeparator() + "workflowDef.getStartTaskDefinition() metadata: " + workflowDef
                        .getStartTaskDefinition().getMetadata().toString()
                        + System.lineSeparator() + "workflowDef.getStartTaskDefinition(): " + workflowDef
                        .getStartTaskDefinition().toString());
            }
        }
    }

    protected void createTestUsersAndGroups() {
        this.transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() {
                if (!authorityService.authorityExists(USER_A)) {
                    personService.createPerson(getUserProperties(USER_A));
                    authenticationService.createAuthentication(USER_A, USER_A.toCharArray());
                    authenticationService.setAuthenticationEnabled(USER_A, true);
                }
                if (!authorityService.authorityExists(USER_B)) {
                    personService.createPerson(getUserProperties(USER_B));
                    authenticationService.createAuthentication(USER_B, USER_B.toCharArray());
                    authenticationService.setAuthenticationEnabled(USER_B, true);
                }
                if (!authorityService.authorityExists(USER_FOREIGNER)) {
                    personService.createPerson(getUserProperties(USER_FOREIGNER));
                    authenticationService.createAuthentication(USER_FOREIGNER, USER_FOREIGNER.toCharArray());
                    authenticationService.setAuthenticationEnabled(USER_FOREIGNER, true);
                }
                if (!authorityService.authorityExists(GROUP_USERS)) {
                    authorityService.createAuthority(AuthorityType.GROUP, GROUP_USERS_shortName);
                    authorityService.addAuthority(GROUP_USERS, USER_A);
                    authorityService.addAuthority(GROUP_USERS, USER_B);
                }
                if (!authorityService.authorityExists(GROUP_FOREIGNERS)) {
                    authorityService.createAuthority(AuthorityType.GROUP, GROUP_FOREIGNERS_shortName);
                    authorityService.addAuthority(GROUP_FOREIGNERS, USER_FOREIGNER);
                }
                return null;
            }
        }, false, true);
    }

    protected Map<QName, Serializable> getUserProperties(String user) {
        Map<QName, Serializable> map = new HashMap<>();
        map.put(ContentModel.PROP_FIRSTNAME, user);
        map.put(ContentModel.PROP_LASTNAME, user);
        map.put(ContentModel.PROP_USERNAME, user);
        map.put(ContentModel.PROP_USER_USERNAME, user);
        map.put(ContentModel.PROP_EMAIL, user + "@localhost");
        map.put(ContentModel.PROP_PASSWORD, user);
        return map;
    }

    protected void setupServices() {
        this.workflowService = this.serviceRegistry.getWorkflowService();
        this.authorityService = this.serviceRegistry.getAuthorityService();
        this.authenticationService = this.serviceRegistry.getAuthenticationService();
        this.personService = this.serviceRegistry.getPersonService();
    }

    protected WorkflowPath createWorkflow(String workflowDefinitionName, String user, String group) {
        logger.debug("Creating workflow from definition " + workflowDefinitionName);
        WorkflowDefinition wfDefinition = this.workflowService.getDefinitionByName(workflowDefinitionName);
        if (wfDefinition == null) {
            Assert.fail("Workflow definition '" + workflowDefinitionName + "' does not exist or is not registered");
            return null;
        }

        logger.debug("Creating parameters");

        NodeRef personNodeRef = authorityService.getAuthorityNodeRef(user);
        NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(group);

        logger.debug("User authority: " + personNodeRef.toString());
        logger.debug("Group authority: " + groupNodeRef.toString());

        Map<QName, Serializable> parameters = new HashMap<>();
        NodeRef sharedFolder = this.getNodeAtPath("/app:company_home/app:shared");
        FileInfo testNode = this.createTestNode(sharedFolder, "temp_" + Long.toString(new Random().nextLong()));
        tempFiles.add(testNode);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, testNode.getNodeRef());
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "This is the description");
        parameters.put(WorkflowModel.PROP_DESCRIPTION, "This is the task description");
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, personNodeRef);
        parameters.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, groupNodeRef);
        parameters.put(WorkflowModel.PROP_PERCENT_COMPLETE, 50);
        parameters.put(WorkflowModel.PROP_REASSIGNABLE, true);
        logger.debug("Done creating parameters. Starting workflow now.");
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), parameters);
        logger.debug("Starting workflow successful");
        return wfPath;
    }

    protected void setOwner(String taskID, String userName) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_OWNER, userName);
        final String finalTaskID = taskID;
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        workflowService.updateTask(finalTaskID, properties, null, null);
                        return null;
                    }
                }, false, true);
    }

    protected boolean apixTaskHasNoOwner(Task task) {
        final String ownerQName = ContentModel.PROP_OWNER.toString();
        Map<String, Serializable> releasedWFTaskProps = task.getProperties();
        if (!releasedWFTaskProps.containsKey(ownerQName)) {
            Assert.fail(ownerQName + " is not in the properties of the task.");
        }
        String ownerValue = (String) releasedWFTaskProps.get(ownerQName);

        boolean apixTaskHasNoOwner = null == ownerValue || ownerValue.isEmpty();
        logger.debug("apixTaskHasNoOwner: " + apixTaskHasNoOwner);
        if (!apixTaskHasNoOwner) {
            logger.debug("apixTaskHasNoOwner ownerValue is " + ownerValue);
        } else if (null != ownerValue && ownerValue.isEmpty()) {
            logger.debug("apixTaskHasNoOwner ownerValue is empty");
        }
        return apixTaskHasNoOwner;
    }

    protected boolean probeClaimAndRelease(String taskID, String currentUserName) {
        boolean apixProbeClaimTask = apixProbeClaimTask(taskID, currentUserName);
        final String finalTaskID = taskID;
        WorkflowTask task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);
        boolean apixProbeReleaseTask = apixProbeReleaseTask(task, currentUserName);
        boolean alfrescoProbeClaimTask = alfrescoProbeClaimTask(taskID, currentUserName);
        boolean alfrescoProbeReleaseTask = alfrescoProbeReleaseTask(taskID, currentUserName);

        logger.debug("probeClaimAndRelease: apixProbeClaimTask: " + apixProbeClaimTask);
        logger.debug("probeClaimAndRelease: apixProbeReleaseTask: " + apixProbeReleaseTask);
        logger.debug("probeClaimAndRelease: alfrescoProbeClaimTask: " + alfrescoProbeClaimTask);
        logger.debug("probeClaimAndRelease: alfrescoProbeReleaseTask: " + alfrescoProbeReleaseTask);

        return apixProbeClaimTask
                && apixProbeReleaseTask
                && alfrescoProbeClaimTask
                && alfrescoProbeReleaseTask;
    }

    protected boolean alfrescoProbeClaimTask(String taskID, String currentUserName) {
        setOwner(taskID, currentUserName);
        final String finalTaskID = taskID;
        WorkflowTask task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);

        boolean notAlfrescoIsClaimable = !alfrescoIsClaimable(task, currentUserName);
        boolean alfrescoIsClaimable = alfrescoIsReleasable(task, currentUserName);
        boolean notApixIsClaimable = !apixIsClaimable(taskID);
        boolean apixIsReleasable = apixIsReleasable(taskID);

        logger.debug("alfrescoProbeClaimTask: notAlfrescoIsClaimable: " + notAlfrescoIsClaimable);
        logger.debug("alfrescoProbeClaimTask: alfrescoIsClaimable: " + alfrescoIsClaimable);
        logger.debug("alfrescoProbeClaimTask: notApixIsClaimable: " + notApixIsClaimable);
        logger.debug("alfrescoProbeClaimTask: apixIsReleasable: " + apixIsReleasable);

        return notAlfrescoIsClaimable
                && alfrescoIsClaimable
                && notApixIsClaimable
                && apixIsReleasable;
    }

    protected boolean alfrescoProbeReleaseTask(String taskID, String currentUserName) {
        setOwner(taskID, null);
        final String finalTaskID = taskID;
        WorkflowTask task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);

        boolean alfrescoIsClaimable = alfrescoIsClaimable(task, currentUserName);
        boolean notAlfrescoIsReleasable = !alfrescoIsReleasable(taskID, currentUserName);

        logger.debug("alfrescoProbeReleaseTask: alfrescoIsClaimable: " + alfrescoIsClaimable);
        logger.debug("alfrescoProbeReleaseTask: notAlfrescoIsReleasable: " + notAlfrescoIsReleasable);

        return alfrescoIsClaimable && notAlfrescoIsReleasable;
    }

    protected boolean alfrescoIsReleasable(String taskID, String username) {
        final String finalTaskID = taskID;
        WorkflowTask task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);
        return alfrescoIsReleasable(task, username);
    }

    protected boolean alfrescoIsReleasable(WorkflowTask wfTask, String username) {
        final WorkflowTask finalWfTask = wfTask;
        final String finalUsername = username;
        logger.debug("------------ " + finalWfTask + "  USER: " + finalUsername);
        boolean alfrescoIsReleasable = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
                    @Override
                    public Boolean execute() {
                        return workflowService.isTaskReleasable(finalWfTask, finalUsername, true);
                    }
                }, false, true);
        logger.debug("alfrescoIsReleasable: " + alfrescoIsReleasable);
        return alfrescoIsReleasable;
    }

    protected boolean alfrescoIsClaimable(WorkflowTask wfTask, String username) {
        final WorkflowTask finalWfTask = wfTask;
        final String finalUsername = username;
        boolean alfrescoIsClaimable = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
                    @Override
                    public Boolean execute() {
                        return workflowService.isTaskClaimable(finalWfTask, finalUsername);
                    }
                }, false, true);
        logger.debug("alfrescoIsClaimable: " + alfrescoIsClaimable);
        return alfrescoIsClaimable;
    }

    protected boolean alfrescoIsClaimable(Task apixWfTask, String username) {
        final String finalTaskID = apixWfTask.getId();
        final WorkflowTask wfTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);

        final String finalUsername = username;
        Boolean alfrescoIsClaimable = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
                    @Override
                    public Boolean execute() {
                        return workflowService.isTaskClaimable(wfTask, finalUsername);
                    }
                }, false, true);

        logger.debug("alfrescoIsClaimable: " + alfrescoIsClaimable);
        return alfrescoIsClaimable;
    }

    protected boolean apixProbeClaimTask(String taskID, String currentUserName) {
        final String finalTaskID = taskID;
        final String finalCurrentUserName = currentUserName;
        Task updatedTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.claimWorkflowTask(finalTaskID, finalCurrentUserName);
                    }
                }, false, true);

        boolean notApixIsClaimable = !apixIsClaimable(updatedTask);
        boolean notAlfrescoIsClaimable = !alfrescoIsClaimable(updatedTask, currentUserName);
        boolean apixIsReleasable = apixIsReleasable(updatedTask);
        boolean alfrescoIsReleasable = alfrescoIsReleasable(updatedTask.getId(), currentUserName);

        logger.debug("apixProbeClaimTask: notApixIsClaimable: " + notApixIsClaimable);
        logger.debug("apixProbeClaimTask: notAlfrescoIsClaimable: " + notAlfrescoIsClaimable);
        logger.debug("apixProbeClaimTask: apixIsReleasable: " + apixIsReleasable);
        logger.debug("apixProbeClaimTask: alfrescoIsReleasable: " + alfrescoIsReleasable);

        return notApixIsClaimable
                && notAlfrescoIsClaimable
                && apixIsReleasable
                && alfrescoIsReleasable;
    }

    protected boolean apixIsClaimable(String taskId) {
        final String finalTaskID = taskId;
        Task task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.getTaskInfo(finalTaskID);
                    }
                }, false, true);
        return apixIsClaimable(task);
    }

    protected boolean apixIsClaimable(Task task) {
        if (!task.getProperties().containsKey(ContentModel.PROP_OWNER.toString())) {
            Assert.fail(ContentModel.PROP_OWNER.toString() + " is not in the properties of the task.");
        }

        boolean isClaimable = (Boolean) task.getProperties().get(IWorkflowService.ALFRESCO_CLAIMABLE);
        boolean apixTaskHasNoOwner = apixTaskHasNoOwner(task);

        logger.debug("apixIsClaimable: isClaimable: " + isClaimable);
        logger.debug("apixIsClaimable: apixTaskHasNoOwner: " + apixTaskHasNoOwner);

        return apixTaskHasNoOwner && isClaimable;
    }

    protected boolean apixProbeReleaseTask(String taskId, String currentUserName) {
        final String finalTaskId = taskId;
        WorkflowTask wfTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskId);
                    }
                }, false, true);
        return apixProbeReleaseTask(wfTask, currentUserName);
    }

    protected boolean apixProbeReleaseTask(WorkflowTask wfTask, String currentUserName) {
        final String wfTaskId = wfTask.getId();
        Task releasedWFTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.releaseWorkflowTask(wfTaskId);
                    }
                }, false, true);

        boolean notApixIsReleasable = !apixIsReleasable(releasedWFTask);
        boolean apixIsClaimable = apixIsClaimable(releasedWFTask);
        boolean notAlfrescoIsReleasable = !alfrescoIsReleasable(wfTask, currentUserName);
        boolean alfrescoIsClaimable = alfrescoIsClaimable(wfTask, currentUserName);

        logger.debug("apixProbeReleaseTask: notApixIsReleasable: " + notApixIsReleasable);
        logger.debug("apixProbeReleaseTask: apixIsClaimable: " + apixIsClaimable);
        logger.debug("apixProbeReleaseTask: notAlfrescoIsReleasable: " + notAlfrescoIsReleasable);
        logger.debug("apixProbeReleaseTask: alfrescoIsClaimable: " + alfrescoIsClaimable);

        return notApixIsReleasable
                && apixIsClaimable
                && notAlfrescoIsReleasable
                && alfrescoIsClaimable;
    }

    protected boolean apixIsReleasable(String taskId) {
        final String wfTaskId = taskId;
        Task task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.getTaskInfo(wfTaskId);
                    }
                }, false, true);
        return apixIsReleasable(task);
    }

    protected boolean apixIsReleasable(Task task) {
        if (!task.getProperties().containsKey(ContentModel.PROP_OWNER.toString())) {
            Assert.fail(ContentModel.PROP_OWNER.toString() + " is not in the properties of the task.");
        }
        boolean isReleasable = (Boolean) task.getProperties().get(IWorkflowService.ALFRESCO_RELEASABLE);
        //boolean apixTaskHasNoOwner = apixTaskHasNoOwner(task);

        logger.debug("apixIsReleasable: isReleasable: " + isReleasable);

        return isReleasable;
    }

    protected List<WorkflowTask> getPooledTasks(String userName) {
        final String finalUserName = userName;
        return this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<WorkflowTask>>() {
                    @Override
                    public List<WorkflowTask> execute() {
                        List<WorkflowTask> result = workflowService.getPooledTasks(finalUserName);
                        logger.debug(
                                "Pooled tasks inside transaction for user '" + finalUserName + "': " + result.size());
                        return result;
                    }
                }, false, true);
    }

    protected boolean probeCancelWorkflow(WorkflowPath workflow) {
        final String workflowId = workflow.getId();
        logger.debug("probeCancelWorkflow: workflowId: " + workflowId);

        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        apixWorkflowService.cancelWorkflow(workflowId);
                        return null;
                    }
                }, false, true);

        WorkflowInstance deletedWorkflow = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowInstance>() {
                    @Override
                    public WorkflowInstance execute() {
                        return workflowService.getWorkflowById(workflowId);
                    }
                }, false, true);

        boolean apixIsWorkflowCanceled_DoesNotExist = deletedWorkflow == null;
        logger.debug(
                "probeCancelWorkflow: apixIsWorkflowCanceled_DoesNotExist: " + apixIsWorkflowCanceled_DoesNotExist);

        return apixIsWorkflowCanceled_DoesNotExist;
    }


    protected boolean hasAccessToWorkflowInstance(WorkflowPath workflow) {
        final String workflowID = workflow.getInstance().getId();
        WorkflowInstance alfrescoWorkflow = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowInstance>() {
                    @Override
                    public WorkflowInstance execute() {
                        return workflowService.getWorkflowById(workflowID);
                    }
                }, false, true);

        if (alfrescoWorkflow == null) {
            return false;
        }
        if (!workflowID.equals(alfrescoWorkflow.getId())) {
            return false;
        }

        Workflow resultWf = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Workflow>() {
                    @Override
                    public Workflow execute() {
                        return apixWorkflowService.getWorkflowInfo(workflowID);
                    }
                }, false, true);
        boolean hasAccessToWorkflowInstance = resultWf != null && workflowID.equals(resultWf.getId());
        logger.debug("hasAccessToWorkflowInstance: " + hasAccessToWorkflowInstance);
        return hasAccessToWorkflowInstance;
    }

    protected boolean hasAccessToWorkflowTask(String taskID) {
        final String finalTaskID = taskID;
        WorkflowTask alfrescoWorkflowTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowTask>() {
                    @Override
                    public WorkflowTask execute() {
                        return workflowService.getTaskById(finalTaskID);
                    }
                }, false, true);

        if (alfrescoWorkflowTask == null) {
            return false;
        }
        if (!taskID.equals(alfrescoWorkflowTask.getId())) {
            return false;
        }

        Task resultWfTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.getTaskInfo(finalTaskID);
                    }
                }, false, true);
        boolean hasAccessToWorkflowTask = resultWfTask != null && taskID.equals(resultWfTask.getId());
        logger.debug("hasAccessToWorkflowTask: " + hasAccessToWorkflowTask);
        return hasAccessToWorkflowTask;
    }

    protected boolean apixProbeUpdateProperties(String taskId, WorkflowOrTaskChanges changes) {
        logger.debug("apixProbeUpdateProperties: taskId: " + taskId);
        logger.debug("apixProbeUpdateProperties: WorkflowOrTaskChanges: ");
        PrintDebugMapOfString2String("apixProbeUpdateProperties input", changes.getPropertiesToSet());

        final String finalTaskId = taskId;
        Map<String, Serializable> oldProperties = this.transactionHelper.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Map<String, Serializable>>() {
                    @Override
                    public Map<String, Serializable> execute() {
                        return apixWorkflowService.getTaskInfo(finalTaskId).getProperties();
                    }
                }, false, true);
        PrintDebugMapOfString2Serializable("apixProbeUpdateProperties oldProperties", oldProperties);

        final WorkflowOrTaskChanges finalChanges = changes;
        Task updatedTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.updateTask(finalTaskId, finalChanges);
                    }
                }, false, true);

        Map<String, Serializable> updatedProperties = updatedTask.getProperties();
        PrintDebugMapOfString2Serializable("apixProbeUpdateProperties updatedProperties", updatedProperties);
        boolean resultAllPropertiesApplied = allPropertiesApplied(oldProperties, updatedProperties,
                new ArrayList<String>(changes.getPropertiesToSet().keySet()));
        logger.debug("apixProbeUpdateProperties: resultAllPropertiesApplied: " + resultAllPropertiesApplied);

        return resultAllPropertiesApplied;
    }

    protected boolean apixProbeUpdateProperties(String taskId, WorkflowOrTaskChanges changes,
            Map<String, String> unmodifiableProperties) {
        logger.debug("apixProbeUpdateProperties: taskId: " + taskId);
        logger.debug("apixProbeUpdateProperties: WorkflowOrTaskChanges: ");
        PrintDebugMapOfString2String("apixProbeUpdateProperties input changes", changes.getPropertiesToSet());
        PrintDebugMapOfString2String("apixProbeUpdateProperties input unmodifiableProperties", unmodifiableProperties);

        final String finalTaskId = taskId;
        Map<String, Serializable> oldProperties = this.transactionHelper.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Map<String, Serializable>>() {
                    @Override
                    public Map<String, Serializable> execute() {
                        return apixWorkflowService.getTaskInfo(finalTaskId).getProperties();
                    }
                }, false, true);
        PrintDebugMapOfString2Serializable("apixProbeUpdateProperties oldProperties", oldProperties);

        final WorkflowOrTaskChanges finalChanges = changes;
        Task updatedTask = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.updateTask(finalTaskId, finalChanges);
                    }
                }, false, true);

        Map<String, Serializable> updatedProperties = updatedTask.getProperties();
        PrintDebugMapOfString2Serializable("apixProbeUpdateProperties updatedProperties", updatedProperties);

        return mixedPropertiesApplied(oldProperties, updatedProperties,
                new ArrayList<>(unmodifiableProperties.keySet()));
    }

    protected boolean allPropertiesApplied(Map<String, Serializable> oldProperties,
            Map<String, Serializable> properties,
            List<String> updatedProperties) {
        if (properties.keySet().size() == 0) {
            logger.debug("allPropertiesApplied: properties is empty! Returning false.");
            return false;
        }

        for (String propertyKey : properties.keySet()) {
            if (!oldProperties.containsKey(propertyKey)) {
                // new property added ? Proceed
                logger.debug("allPropertiesApplied: oldProperties does not contain " + propertyKey);
                continue;
            }

            if (!updatedProperties.contains(propertyKey)) {
                // property out of scope
                logger.debug("allPropertiesApplied: updatedProperties does not contain " + propertyKey);
                continue;
            }

            Serializable oldValue = oldProperties.get(propertyKey);
            Serializable newValue = properties.get(propertyKey);

            if (Objects.equals(oldValue, newValue)) {
                logger.debug(
                        propertyKey + " did not change! Old value: '" + oldValue + "' ; New value: '" + newValue + "'");
                // A property has not been updated?
                return false;
            }
        }

        // All properties have changed.
        return true;
    }

    protected boolean allPropertiesApplied2(Map<String, Serializable> oldProperties,
            Map<String, Serializable> changedProperties,
            List<String> updatedProperties) {
        if (changedProperties.keySet().size() == 0) {
            logger.debug("allPropertiesApplied: changedProperties is empty! Returning false.");
            return false;
        }

        for (String propertyKey : changedProperties.keySet()) {
            if (!oldProperties.containsKey(propertyKey)) {
                // new property added ? Proceed
                logger.debug("allPropertiesApplied: oldProperties does not contain " + propertyKey);
                continue;
            }

            if (!updatedProperties.contains(propertyKey)) {
                // property out of scope
                logger.debug("allPropertiesApplied: updatedProperties does not contain " + propertyKey);
                continue;
            }

            Serializable oldValue = oldProperties.get(propertyKey);
            Serializable newValue = changedProperties.get(propertyKey);

            if (Objects.equals(oldValue, newValue)) {
                logger.debug(
                        propertyKey + " did not change! Old value: '" + oldValue + "' ; New value: '" + newValue + "'");
                // A property has not been updated?
                return false;
            }
        }

        // All properties have changed.
        return true;
    }

    protected boolean noPropertiesApplied(Map<String, Serializable> oldProperties,
            Map<String, Serializable> properties) {
        for (String propertyKey : properties.keySet()) {
            if (!oldProperties.containsKey(propertyKey)) {
                // new property added?
                return false;
            }

            Serializable oldValue = oldProperties.get(propertyKey);
            Serializable newValue = properties.get(propertyKey);

            if (!Objects.equals(oldValue, newValue)) {
                // A property has been updated?
                return false;
            }
        }
        // No properties changed.
        return true;
    }

    protected boolean mixedPropertiesApplied(Map<String, Serializable> oldProperties,
            Map<String, Serializable> properties, List<String> unmodifiableProperties) {
        boolean newPropertyAdded = false;
        boolean oldModifiablePropertyChanged = false;
        boolean oldUnmodifiablePropertyChanged = false;

        for (String propertyKey : properties.keySet()) {
            if (!oldProperties.containsKey(propertyKey)) {
                // new property added ? Proceed
                newPropertyAdded = true;
                continue;
            }

            if (unmodifiableProperties.contains(propertyKey)) {
                if (hasChanged(propertyKey, properties, oldProperties)) {
                    oldUnmodifiablePropertyChanged = true;
                    logger.debug("mixedPropertiesApplied: oldUnmodifiablePropertyChanged: '" + propertyKey + "' "
                            + oldUnmodifiablePropertyChanged);
                    logger.debug("mixedPropertiesApplied: Old value: '" + propertyKey + "' " + oldProperties
                            .get(propertyKey));
                    logger.debug(
                            "mixedPropertiesApplied: New value: '" + propertyKey + "' " + properties.get(propertyKey));
                }
                continue;
            }

            if (hasChanged(propertyKey, properties, oldProperties)) {
                oldModifiablePropertyChanged = true;
            }
        }

        logger.debug("mixedPropertiesApplied: newPropertyAdded: " + newPropertyAdded);
        logger.debug("mixedPropertiesApplied: oldModifiablePropertyChanged: " + oldModifiablePropertyChanged);
        logger.debug("mixedPropertiesApplied: oldUnmodifiablePropertyChanged: " + oldUnmodifiablePropertyChanged);

        return (newPropertyAdded || oldModifiablePropertyChanged) && !oldUnmodifiablePropertyChanged;
    }

    private boolean hasChanged(String propertyKey, Map<String, Serializable> properties,
            Map<String, Serializable> oldProperties) {
        Serializable oldValue = oldProperties.get(propertyKey);
        Serializable newValue = properties.get(propertyKey);

        return !Objects.equals(oldValue, newValue);
    }

    private void PrintDebugMapOfString2String(String methodName, Map<String, String> propertiesToSet) {
        for (String propertyKey : propertiesToSet.keySet()) {
            logger.debug(methodName + ": Property: '" + propertyKey + "': '" + propertiesToSet.get(propertyKey) + "'");
        }
    }

    private void PrintDebugMapOfString2Serializable(String methodName, Map<String, Serializable> propertiesToSet) {
        for (String propertyKey : propertiesToSet.keySet()) {
            Serializable serializableValue = propertiesToSet.get(propertyKey);
            if (null == serializableValue) {
                logger.debug(methodName + ": Property: '" + propertyKey + "' is null");
                continue;
            }
            logger.debug(methodName + ": Property: '" + propertyKey + "': '" + serializableValue.toString() + "'");
        }
    }

    protected void probeApixTransitionTask(String transition, String taskID) {
        logger.debug("probeApixTransitionTask: " + taskID + " : " + transition);

        final String finalTaskID = taskID;
        final String finalTransitionID = transition;

        this.transactionHelper.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Map<String, Serializable>>() {
                    @Override
                    public Map<String, Serializable> execute() {
                        apixWorkflowService.endTask(finalTaskID, finalTransitionID);
                        return null;
                    }
                }, false, true);
    }

    protected void initTransitionsAndTasks(String methodName, String sourceTaskID,
            Map<String, List<String>> targetTasksMap) {
        final Map<String, Map<String, Map<String, WorkflowPath>>> localTasksToMap = new HashMap<>();
        final String finalSourceTaskID = sourceTaskID;
        Task task = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Task>() {
                    @Override
                    public Task execute() {
                        return apixWorkflowService.getTaskInfo(finalSourceTaskID);
                    }
                }, false, true);
        logger.debug("initTransitionsAndTasks: task found: " + (task != null));
        Serializable transitionProperty = task.getProperties().get(IWorkflowService.ALFRESCO_TRANSITIONS);
        logger.debug("initTransitionsAndTasks: transitionProperty found: " + (transitionProperty != null));
        final ArrayList<WorkflowTransition> transitions = (ArrayList<WorkflowTransition>) transitionProperty;
        final String finalMethodName = methodName;
        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        for (final WorkflowTransition transition : transitions) {
                            localTasksToMap.put(finalMethodName,
                                    new HashMap<String, Map<String, WorkflowPath>>() {{
                                        put(transition.getTitle(),
                                                createSampleWorkflowUserMethodMap(ACTIVITI_POOLED_REVIEW, USER_A,
                                                        GROUP_USERS));
                                    }});
                        }
                        return null;
                    }
                }, false, true);

        extractTransactionsAndTasksFromWorkflows(localTasksToMap, targetTasksMap);
    }

    protected void extractTransactionsAndTasksFromWorkflows(
            Map<String, Map<String, Map<String, WorkflowPath>>> sourceWorkflows,
            Map<String, List<String>> targetTasksMap) {
        final Map<String, Map<String, Map<String, WorkflowPath>>> localTasksToMap = sourceWorkflows;
        final Map<String, List<String>> finalTargetTasksMap = targetTasksMap;

        Object result = this.transactionHelper
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() {
                        for (String methodName : localTasksToMap.keySet()) {
                            logger.debug("Mapping task to method '" + methodName + "'");
                            Map<String, Map<String, WorkflowPath>> transitionsToWorkflowsMap = localTasksToMap
                                    .get(methodName);
                            for (String transition : transitionsToWorkflowsMap.keySet()) {
                                logger.debug("Mapping workflow path to transition '" + transition + "'");
                                Map<String, WorkflowPath> userToWorkflowMap = transitionsToWorkflowsMap.get(transition);
                                for (String user : userToWorkflowMap.keySet()) {
                                    logger.debug("Mapping workflow path to user '" + user + "'");
                                    List<String> taskIds = getTask(userToWorkflowMap.get(user), user);
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

    protected TaskOrWorkflowSearchResult getSearchResultTasks(TaskSearchQuery searchQuery) {
        final TaskSearchQuery finalSearchQuery = searchQuery;
        return this.transactionHelper.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<TaskOrWorkflowSearchResult>() {
                    @Override
                    public TaskOrWorkflowSearchResult execute() {
                        return apixWorkflowService.searchTasks(finalSearchQuery);
                    }
                }, false, true);
    }

    protected TaskSearchQuery createNewSearchQuery(TaskSearchQuery.QueryScope scope, int skip, int limit,
            String sortingOrder, String sortingProperty) {
        Paging paging = new Paging();
        paging.skip = skip;
        paging.limit = limit;

        Sorting sorting = new Sorting();
        sorting.order = sortingOrder;
        sorting.property = sortingProperty;

        TaskSearchQuery searchQuery = new TaskSearchQuery();
        searchQuery.scope = scope;
        searchQuery.facets = new ArrayList<>();
        searchQuery.filters = new ArrayList<>();
        searchQuery.includeRefs = false;
        searchQuery.includeResults = true;
        searchQuery.paging = paging;
        searchQuery.orderBy = new ArrayList<>();
        searchQuery.orderBy.add(sorting);

        return searchQuery;
    }

    //</editor-fold>

    protected void cleanUpLocal() {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        this.cleanUpUsersAndGroups();
        this.cleanUpWorkflows();
        this.cleanUpTasks();
        this.cleanUpAllWorkflows();
        this.removeTempFiles();
        super.cleanUp();
    }

    private void removeTempFiles() {
        logger.debug("cleaning up temp files");
        for (FileInfo tempFile : tempFiles) {
            this.removeTestNode(tempFile.getNodeRef());
        }
    }

    private void cleanUpUsersAndGroups() {
        logger.debug("cleaning up users");
        if (this.authorityService.authorityExists(USER_A)) {
            this.personService.deletePerson(USER_A);
        }
        if (this.authorityService.authorityExists(USER_B)) {
            this.personService.deletePerson(USER_B);
        }
        if (this.authorityService.authorityExists(USER_FOREIGNER)) {
            this.personService.deletePerson(USER_FOREIGNER);
        }
        if (this.authorityService.authorityExists(GROUP_USERS)) {
            this.authorityService.deleteAuthority(GROUP_USERS);
        }
        if (this.authorityService.authorityExists(GROUP_FOREIGNERS)) {
            this.authorityService.deleteAuthority(GROUP_FOREIGNERS);
        }
    }

    private void cleanUpWorkflows() {
        logger.debug("cleaning up workflows");
        for (String testMethod : this.wfPaths.keySet()) {
            WorkflowPath wfPath = this.wfPaths.get(testMethod);
            WorkflowInstance workflow = this.workflowService.getWorkflowById(wfPath.getInstance().getId());
            if (workflow != null) {
                this.workflowService.deleteWorkflow(workflow.getId());
            }
        }
        wfPaths.clear();
    }

    private void cleanUpTasks() {
        logger.debug("cleaning up tasks");
        for (String testMethod : this.tasks.keySet()) {
            String wfTaskID = this.tasks.get(testMethod);
            WorkflowTask task = this.workflowService.getTaskById(wfTaskID);
            if (task == null) {
                logger.debug("Task does not exist anymore: " + wfTaskID);
                continue;
            }
            String wfID = task.getPath().getInstance().getId();
            WorkflowInstance workflow = this.workflowService.getWorkflowById(wfID);
            if (workflow == null) {
                logger.debug("Workflow does not exist anymore: " + wfTaskID);
                continue;
            }
            this.workflowService.deleteWorkflow(wfID);
        }
        tasks.clear();
    }

    protected void cleanUpAllWorkflows() {
        logger.debug("cleaning up all workflows");
        List<WorkflowInstance> activeWorkflows = this.workflowService.getActiveWorkflows();
        for (WorkflowInstance workflowInstance : activeWorkflows) {
            WorkflowInstance workflow = this.workflowService.getWorkflowById(workflowInstance.getId());
            if (workflow == null) {
                logger.debug("Workflow does not exist anymore: " + workflowInstance.getId());
                continue;
            }
            this.workflowService.deleteWorkflow(workflowInstance.getId());
        }
        List<WorkflowInstance> completedWorkflows = this.workflowService.getCompletedWorkflows();
        for (WorkflowInstance workflowInstance : completedWorkflows) {
            WorkflowInstance workflow = this.workflowService.getWorkflowById(workflowInstance.getId());
            if (workflow == null) {
                logger.debug("Workflow does not exist anymore: " + workflowInstance.getId());
                continue;
            }
            this.workflowService.deleteWorkflow(workflowInstance.getId());
        }
    }
}
