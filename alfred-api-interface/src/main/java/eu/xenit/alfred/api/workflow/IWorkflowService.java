package eu.xenit.alfred.api.workflow;

import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.workflow.model.Task;
import eu.xenit.alfred.api.workflow.model.Workflow;
import eu.xenit.alfred.api.workflow.model.WorkflowDefinition;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.TaskSearchQuery;
import eu.xenit.alfred.api.workflow.search.WorkflowSearchQuery;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A service that carries out all activities related to {@link Workflow workflow} instances
 * / workflow {@link Task tasks}
 */
public interface IWorkflowService {

    ///<editor-fold comment="BPM Workflow instance/task properties' Full QNames">
    // TODO Refactor to dictionary service or smth?
    String ALFRESCO_ACTIVE = "{http://www.alfresco.org/model/bpm/1.0}active";
    String ALFRESCO_OWNER = "{http://www.alfresco.org/model/content/1.0}owner";
    String ALFRESCO_COMPLETIONDATE = "{http://www.alfresco.org/model/bpm/1.0}completionDate";
    String ALFRESCO_ENDDATE = "{http://www.alfresco.org/model/bpm/1.0}endDate";
    String ALFRESCO_STARTDATE = "{http://www.alfresco.org/model/bpm/1.0}startDate";
    String ALFRESCO_DUEDATE = "{http://www.alfresco.org/model/bpm/1.0}dueDate";
    String ALFRESCO_WF_DUEDATE = "{http://www.alfresco.org/model/bpm/1.0}workflowDueDate";
    String ALFRESCO_ID = "{http://www.alfresco.org/model/bpm/1.0}id";
    String ALFRESCO_INITIATOR = "{http://www.alfresco.org/model/bpm/1.0}initiator";
    String ALFRESCO_BPM_NAME = "{http://www.alfresco.org/model/bpm/1.0}name";
    String ALFRESCO_CM_NAME = "{http://www.alfresco.org/model/content/1.0}name";
    String ALFRESCO_VERSION = "{http://www.alfresco.org/model/bpm/1.0}version";
    String ALFRESCO_PRIORITY = "{http://www.alfresco.org/model/bpm/1.0}priority";
    String ALFRESCO_WF_PRIORITY = "{http://www.alfresco.org/model/bpm/1.0}workflowPriority";
    String ALFRESCO_TITLE = "{http://www.alfresco.org/model/bpm/1.0}title";
    String ALFRESCO_WORKFLOW_PACKAGE = "{http://www.alfresco.org/model/bpm/1.0}workflowPackage";
    String ALFRESCO_CONTEXT = "{http://www.alfresco.org/model/bpm/1.0}context";
    String ALFRESCO_DESCRIPTION = "{http://www.alfresco.org/model/bpm/1.0}description";
    String ALFRESCO_CLAIMABLE = "{http://www.alfresco.org/model/bpm/1.0}claimable";
    String ALFRESCO_RELEASABLE = "{http://www.alfresco.org/model/bpm/1.0}releasable";
    String ALFRESCO_TYPE = "{http://www.alfresco.org/model/bpm/1.0}type";
    String ALFRESCO_DEFINITION = "{http://www.alfresco.org/model/bpm/1.0}definition";
    String ALFRESCO_STATE = "{http://www.alfresco.org/model/bpm/1.0}state";
    String ALFRESCO_TRANSITIONS = "{http://www.alfresco.org/model/bpm/1.0}transitions";
    String ALFRESCO_WORKFLOW_ID = "{http://www.alfresco.org/model/bpm/1.0}workflowId";
    String ALFRESCO_WORKFLOW_INSTANCE_ID = "{http://www.alfresco.org/model/bpm/1.0}workflowInstanceId";
    String ALFRESCO_ASSIGNEE = "{http://www.alfresco.org/model/bpm/1.0}assignee";
    String ALFRESCO_GROUP_ASSIGNEE = "{http://www.alfresco.org/model/bpm/1.0}groupAssignee";
    String ALFRESCO_INVOLVED = "{http://www.alfresco.org/model/bpm/1.0}involved";
    ///</editor-fold>

    /**
     * Gets the information of the workflow instances that match the provided search criteria
     *
     * @param workflowSearchQuery This parameter contains the (API-x model) search criteria for the workflow instances
     * to be retrieved
     * @return Returns a TaskOrWorkflowSearchResult object containing the information of the workflow instances to be
     * returned. These workflow instances were found by using the WorkflowSearchQuery provided
     */
    TaskOrWorkflowSearchResult searchWorkflows(WorkflowSearchQuery workflowSearchQuery);

    /**
     * Gets the information of the workflow tasks that match the provided search criteria
     *
     * @param taskSearchQuery This parameter contains the (API-x model) search criteria for the workflow tasks to be
     * retrieved
     * @return Returns a TaskOrWorkflowSearchResult object containing the information of the workflow tasks to be
     * returned. These workflow tasks were found by using the WorkflowSearchQuery provided
     */
    TaskOrWorkflowSearchResult searchTasks(TaskSearchQuery taskSearchQuery);

    /**
     * Gets the information of the workflow task that has the provided id
     *
     * @param taskID Specifies the id of the requested workflow task
     * @return Returns a workflow task instance with the provided id
     */
    Task getTaskInfo(String taskID);

    /**
     * Gets the information of the workflow instance that has the provided id
     *
     * @param workflowID Specifies the id of the requested workflow instance
     * @return Returns a workflow instance instance with the provided id
     */
    Workflow getWorkflowInfo(String workflowID);

    /**
     * Gets a list of all workflow definitions that are deployed
     *
     * @return A list of all workflow definitions that are deployed
     */
    List<WorkflowDefinition> getAllDefinitions();

    /**
     * End the task with the provided id, by using the provided transitionID to specify in which way/transition the task
     * has ended. This could be for example transitionID 'Next' that describes 'Task Done' transition. Other examples
     * are 'Approve', 'Reject', etc.
     *
     * @param taskID Specifies the id of the workflow task to be ended
     * @param transitionID Specifies the id of the transition that describes how the task ended
     */
    void endTask(String taskID, String transitionID);

    /**
     * [Deprecated] Updates the workflow instance that has the provided id with the provided set of the changed
     * properties with their values.
     *
     * @param id Specifies the id of the workflow instance that is to be updated
     * @param changes A hash map that contains the full {@link QName} of the changed properties and
     * their {@link java.io.Serializable} values
     * @return The updated Workflow instance instance
     */
    Workflow updateWorkflow(String id, WorkflowOrTaskChanges changes);

    /**
     * Updates the workflow task that has the provided id with the provided set of the changed properties with their
     * values.
     *
     * @param id Specifies the id of the workflow task that is to be updated
     * @param changes A hash map that contains the full {@link QName} of the changed properties and
     * their {@link java.io.Serializable} values
     * @return The updated workflow Task instance
     */
    Task updateTask(String id, WorkflowOrTaskChanges changes);

    /**
     * Claim the task with the provided id. The current user will be used as the claimer.
     *
     * @param taskID Specifies the id of the workflow task that is to be claimed
     * @return The claimed workflow Task instance
     */
    Task claimWorkflowTask(String taskID);

    /**
     * Claim the task with the provided id. The user name provided will be used to find the claimer user.
     *
     * @param taskID Specifies the id of the workflow task that is to be claimed
     * @param userName Specifies the user name of the user that will claim the task
     * @return The claimed workflow Task instance
     */
    Task claimWorkflowTask(String taskID, String userName);

    /**
     * Release the task to pool of {@link Person users} and/or
     * {@link Person groups} with the provided id.
     *
     * @param taskID Specifies the id of the workflow {@link Task task} that is to be
     * released
     * @return The released workflow Task instance
     */
    Task releaseWorkflowTask(String taskID);

    /**
     * [DEV] To be used for development/testing purposes only! Generates sample workflow based on the first workflow
     * definition available.
     *
     * @param amount How many workflow instances to generate
     * @param username To who will these workflow instances be assigned
     */
    void GenerateWorkflows(int amount, String username);

    void cancelWorkflow(String id);

    WorkflowDefinition getWorkflowDefinition(String workflowName);

    /**
     * Starts one instance of the workflow of the given definition
     *
     * @param definitionId The id defining a workflow definition
     * @param parameters Starting parameters for workflow
     * @return POJO containing workflow instance id
     */
    Workflow startWorkflow(String definitionId, Map<String, Serializable> parameters);


}
