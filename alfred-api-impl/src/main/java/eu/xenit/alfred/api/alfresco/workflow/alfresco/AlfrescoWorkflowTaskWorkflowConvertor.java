package eu.xenit.alfred.api.alfresco.workflow.alfresco;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiAlfrescoWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.AbstractAlfredApiWorkflowConvertor;
import eu.xenit.alfred.api.alfresco.workflow.activiti.ActivitiWorkflowTaskWorkflowConvertor;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.model.ITaskOrWorkflow;
import eu.xenit.alfred.api.workflow.model.Task;
import eu.xenit.alfred.api.workflow.model.TaskDefinition;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor")
public class AlfrescoWorkflowTaskWorkflowConvertor extends AbstractAlfredApiAlfrescoWorkflowConvertor {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoWorkflowTaskWorkflowConvertor.class);

    @Autowired
    public AlfrescoWorkflowTaskWorkflowConvertor(
            ServiceRegistry serviceRegistry,
            IPeopleService peopleService,
            AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion) {
        super(serviceRegistry, peopleService, alfredApiToAlfrescoConversion);
    }

    public <T> String getId(T task) {
        return ((WorkflowTask) task).getId();
    }

    public ITaskOrWorkflow apply(String id) {
        try {
            WorkflowTask alfrescoTask = this.workflowService.getTaskById(id);
            return this.apply(alfrescoTask);
        } catch (AccessDeniedException var4) {
            logger.warn("Access Denied to task instance with ID " + id);
            return null;
        }
    }

    public <T> Task apply(T obj) {
        WorkflowTask task = (WorkflowTask) obj;
        if (task == null) {
            return null;
        }

        Task ret = new Task();
        ret.setId(task.getId());
        String state = task.getState().name();
        WorkflowTaskDefinition tD = task.getDefinition();
        ArrayList<WorkflowTransition> transitions = getTaskTransitions(task);
        TaskDefinition definition = new TaskDefinition(tD.getId(), tD.getMetadata().getProperties().keySet());
        Map<String, Serializable> props = toAlfredApiProperties(task.getProperties());
        String workflowId = task.getPath().getInstance().getId();

        eu.xenit.alfred.api.data.NodeRef alfredApiAssignee = null;
        Serializable assigneeSerializable = task.getProperties().get(QNAME_ASSIGNEE);
        if (assigneeSerializable != null) {
            logger.debug(
                    "eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor.apply() assigneeNodeRef: "
                            + assigneeSerializable.toString());
            if (assigneeSerializable instanceof String) {
                String assigneeName = (String) assigneeSerializable;
                alfredApiAssignee = this.getUserNodeRef(assigneeName);
            } else if (assigneeSerializable instanceof NodeRef) {
                NodeRef assigneeNodeRef = (NodeRef) assigneeSerializable;
                alfredApiAssignee = c.alfredApi(assigneeNodeRef);
            }

        }

        eu.xenit.alfred.api.data.NodeRef alfredApiGroupAssignee = null;
        Serializable groupNodeRef = task.getProperties().get(QNAME_GROUP_ASSIGNEE);
        if (groupNodeRef != null) {
            logger.debug(
                    "eu.xenit.alfred.api.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor.apply() groupNodeRef: "
                            + groupNodeRef.toString());
            NodeRef groupAssignee = (NodeRef) groupNodeRef;
            alfredApiGroupAssignee = this.c.alfredApi(groupAssignee);
        }

        String user = getCurrentUser();
        boolean isTaskClaimable = this.workflowService.isTaskClaimable(task, user);
        boolean isTaskReleasable = this.workflowService.isTaskReleasable(task, user);

        String description = getDescription(task);

        PutOnMapIfNotNull(props, WorkflowModel.ASSOC_ASSIGNEE, alfredApiAssignee);
        PutOnMapIfNotNull(props, WorkflowModel.PROP_DESCRIPTION, description);
        PutOnMapIfNotNull(props, WorkflowModel.TYPE_TASK, task.getName());
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_DEFINITION, definition);
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_STATE, state);
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_TITLE, task.getTitle());
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_TRANSITIONS, transitions);
        PutOnMapIfNotNull(props, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, workflowId);
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_GROUP_ASSIGNEE, alfredApiGroupAssignee);
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_CLAIMABLE, isTaskClaimable);
        PutOnMapIfNotNull(props, IWorkflowService.ALFRESCO_RELEASABLE, isTaskReleasable);
        ret.setProperties(props);
        return ret;
    }

    private String getDescription(WorkflowTask task) {
        if (task.getProperties().containsKey(WorkflowModel.PROP_DESCRIPTION)) {
            Object descriptionProperty = task.getProperties().get(WorkflowModel.PROP_DESCRIPTION);
            if (descriptionProperty != null) {
                return descriptionProperty.toString();
            }
        }
        String taskDescription = task.getDescription();
        String workflowDefinitionDescription = task.getPath().getInstance().getDefinition().getDescription();
        String workflowDescription = task.getPath().getInstance().getDescription();
        return taskDescription.equals(workflowDefinitionDescription) ? workflowDescription : taskDescription;
    }

    public void update(String id, WorkflowOrTaskChanges changes) {
        assertCanModify(id);
        Map<QName, Serializable> changesToSet = filterEditableProperties(changes);
        if (changesToSet.isEmpty()) {
            return;
        }
        HashMap<QName, List<NodeRef>> emptyList = new HashMap<>();
        this.workflowService.updateTask(id, changesToSet, emptyList, emptyList);
    }

    public void generate(int amount, String username) {
        throw new Error("Not implemented yet!");
    }

    public void end(String taskID, String transitionID) {
        assertCanModify(taskID);
        this.workflowService.endTask(taskID, transitionID);
    }

    public void claim(String taskID) {
        this.claim(taskID, getCurrentUser());
    }

    public void claim(String taskID, String userName) {
        logger.debug("given taskID: " + taskID);
        logger.debug("given userName: " + userName);
        if (!taskID.startsWith(ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX)) {
            logger.debug("taskID doesn't start with " + ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX);
            taskID = ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX + taskID;
            logger.debug("new taskID: " + taskID);
        }

        WorkflowTask wfTask = this.workflowService.getTaskById(taskID);
        if (wfTask == null) {
            throw new RuntimeException("Workflow task with id " + taskID + " not found");
        }

        if (!this.isAdmin(userName)) {
            if (!this.workflowService.isTaskClaimable(wfTask, userName)) {
                throw new RuntimeException("Workflow task with id " + wfTask.getId() + " is not claimable");
            }
        }

        setOwner(this.workflowService, taskID, userName);
    }

    public void release(String taskID) {
        assertCanModify(taskID);
        String userName = getCurrentUser();
        if (!taskID.startsWith(ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX)) {
            taskID = ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX + taskID;
        }

        WorkflowTask wfTask = this.workflowService.getTaskById(taskID);
        if (wfTask == null) {
            throw new RuntimeException("Workflow task with id " + taskID + " not found");
        }

        if (!this.isAdmin(userName)) {
            if (!this.workflowService.isTaskReleasable(wfTask, userName)) {
                throw new RuntimeException("Workflow task with id " + taskID + " is not releasable");
            }
        }

        setOwner(this.workflowService, taskID, null);
    }
}
