package eu.xenit.apix.alfresco.workflow.alfresco;

import eu.xenit.apix.alfresco.workflow.AbstractApixAlfrescoWorkflowConvertor;
import eu.xenit.apix.alfresco.workflow.AbstractApixWorkflowConvertor;
import eu.xenit.apix.workflow.model.ITaskOrWorkflow;
import eu.xenit.apix.workflow.model.Task;
import eu.xenit.apix.workflow.model.Workflow;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.alfresco.workflow.alfresco.AlfrescoProcessInstanceWorkflowConvertor")
public class AlfrescoProcessInstanceWorkflowConvertor extends AbstractApixAlfrescoWorkflowConvertor {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoProcessInstanceWorkflowConvertor.class);
    private static final Random random = new Random();
    @Autowired
    protected PersonService personService;
    @Autowired // missing?
    @Qualifier("eu.xenit.apix.alfresco.workflow.alfresco.AlfrescoWorkflowTaskWorkflowConvertor")
    protected AbstractApixWorkflowConvertor taskConvertor;

    public <T> String getId(T instance) {
        return ((WorkflowInstance) instance).getId();
    }

    public ITaskOrWorkflow apply(String id) {
        WorkflowInstance workflowInstance = this.workflowService.getWorkflowById(id);

        try {
            WorkflowTask startTask = this.workflowService.getStartTask(workflowInstance.getId());
            return this.apply(workflowInstance, startTask);
        } catch (AccessDeniedException var4) {
            logger.warn("Access Denied to process instance with ID " + id);
            return null;
        }
    }

    public <T> ITaskOrWorkflow apply(T instance) {
        return this.apply(this.getId(instance));
    }

    private Workflow apply(WorkflowInstance instance, WorkflowTask startTask) {
        if (instance == null) {
            return null;
        } else {
            logger.error("instance.getId() {} , this.taskConvertor {}     startTask {}", instance.getId(), this.taskConvertor, startTask);
            Workflow ret = new Workflow();
            ret.setId(instance.getId());
            Task startTaskApix = (Task) this.taskConvertor.apply(startTask);
            logger.error("startTaskApix {}, id {}" , startTaskApix.getProperties(), startTaskApix.getId());
            Map<String, Serializable> props = startTaskApix.getProperties();
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}active", instance.isActive());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}context", instance.getContext());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}description", instance.getDescription());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}dueDate", instance.getDueDate());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}endDate", instance.getEndDate());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}startDate", instance.getStartDate());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}id", ret.id);
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}initiator",
                    this.c.apix(instance.getInitiator()));
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/content/1.0}name",
                    instance.getDefinition().getName());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}version",
                    instance.getDefinition().getVersion());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}priority", instance.getPriority());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}title",
                    instance.getDefinition().getTitle());
            PutOnMapIfNotNull(props, "{http://www.alfresco.org/model/bpm/1.0}workflowPackage",
                    instance.getWorkflowPackage());
            ret.setProperties(props);
            return ret;
        }
    }

    public void update(String id, WorkflowOrTaskChanges changes) {
        throw new NotImplementedException("Method not yet implemented.");
    }

    public void generate(int amount, String username) {
        for (int i = 0; i < amount; ++i) {
            String randomComment = "" + random.nextLong();
            WorkflowDefinition d = this.workflowService.getDefinitions().get(0);
            Map<QName, Serializable> variables = new HashMap<>();
            variables.put(WorkflowModel.PROP_COMMENT, randomComment);
            variables.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, new Date());
            variables.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, randomComment);
            variables.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, "true");
            variables.put(WorkflowModel.ASSOC_ASSIGNEE, this.personService.getPerson(username));
            this.workflowService.startWorkflow(d.getId(), variables);
        }

    }

    public void end(String taskID, String transitionID) {
        throw new NotImplementedException("Method not yet implemented.");
    }

    public void claim(String taskID) {
        throw new NotImplementedException("Method not yet implemented.");
    }

    public void claim(String taskID, String username) {
        throw new NotImplementedException("Method not yet implemented.");
    }

    public void release(String taskID) {
        throw new NotImplementedException("Method not yet implemented.");
    }
}
