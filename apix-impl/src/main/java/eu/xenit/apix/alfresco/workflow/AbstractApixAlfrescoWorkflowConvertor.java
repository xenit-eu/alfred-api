package eu.xenit.apix.alfresco.workflow;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractApixAlfrescoWorkflowConvertor extends AbstractApixWorkflowConvertor {

    protected static final QName QNAME_ASSIGNEE = QName.createQName(IWorkflowService.ALFRESCO_ASSIGNEE);
    protected static final QName QNAME_GROUP_ASSIGNEE = QName.createQName(IWorkflowService.ALFRESCO_GROUP_ASSIGNEE);
    private static final Logger logger = LoggerFactory.getLogger(AbstractApixAlfrescoWorkflowConvertor.class);
    private static final QName REVIEW_OUTCOME_PROPERTY = QName
            .createQName("http://www.alfresco.org/model/bpm/1.0", "outcomePropertyName");
    private static final String LIST_CONSTRAINT = "listconstraint";
    private static final String OPTIONS_LABEL = "Options";
    private static final String ALLOWED_VALUES = "allowedValues";
    private static final QName ownerQName = QName.createQName(IWorkflowService.ALFRESCO_OWNER);
    private static final QName initiatorQName = QName.createQName(IWorkflowService.ALFRESCO_INITIATOR);
    private static final Map<String, String> ApixToAlfrescoNames = new HashMap<String, String>() {{
        put(IWorkflowService.ALFRESCO_DUEDATE, IWorkflowService.ALFRESCO_DUEDATE);
        put(IWorkflowService.ALFRESCO_ENDDATE, IWorkflowService.ALFRESCO_ENDDATE);
        put(IWorkflowService.ALFRESCO_STARTDATE, IWorkflowService.ALFRESCO_STARTDATE);
        put(IWorkflowService.ALFRESCO_PRIORITY, IWorkflowService.ALFRESCO_PRIORITY);
        put(IWorkflowService.ALFRESCO_TITLE, IWorkflowService.ALFRESCO_TITLE);
        put(IWorkflowService.ALFRESCO_DESCRIPTION, IWorkflowService.ALFRESCO_DESCRIPTION);
    }};
    private static final List<String> nodeRefProperties = new ArrayList<String>() {{
        add(IWorkflowService.ALFRESCO_GROUP_ASSIGNEE);
        add(IWorkflowService.ALFRESCO_ASSIGNEE);
        add(IWorkflowService.ALFRESCO_INITIATOR);
        add(IWorkflowService.ALFRESCO_OWNER);
    }};
    private static final List<String> dateProperties = new ArrayList<String>() {{
        add(IWorkflowService.ALFRESCO_DUEDATE);
        add(IWorkflowService.ALFRESCO_ENDDATE);
        add(IWorkflowService.ALFRESCO_STARTDATE);
    }};
    private static final List<String> integerProperties = new ArrayList<String>() {{
        add(IWorkflowService.ALFRESCO_PRIORITY);
    }};
    protected static HashSet<String> uneditableProps = new HashSet<String>() {{
        add(IWorkflowService.ALFRESCO_CONTEXT);
        add(IWorkflowService.ALFRESCO_DESCRIPTION);
        add(IWorkflowService.ALFRESCO_ACTIVE);
        add(IWorkflowService.ALFRESCO_ID);
        add(IWorkflowService.ALFRESCO_INITIATOR);
        add(IWorkflowService.ALFRESCO_BPM_NAME);
        add(IWorkflowService.ALFRESCO_VERSION);
        add(IWorkflowService.ALFRESCO_WORKFLOW_PACKAGE);
        add(IWorkflowService.ALFRESCO_CLAIMABLE);
        add(IWorkflowService.ALFRESCO_RELEASABLE);
        add(IWorkflowService.ALFRESCO_TYPE);
        add(IWorkflowService.ALFRESCO_DEFINITION);
        add(IWorkflowService.ALFRESCO_STATE);
        add(IWorkflowService.ALFRESCO_TRANSITIONS);
        add(IWorkflowService.ALFRESCO_WORKFLOW_ID);
    }};
    @Autowired
    protected ApixToAlfrescoConversion c;
    @Autowired
    protected WorkflowService workflowService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private NamespaceService namespaceService;

    protected static void setOwner(final WorkflowService ws, final String taskID, final String userName) {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>() {{
            put(ContentModel.PROP_OWNER, userName);
        }};
        ws.updateTask(taskID, properties, null, null);
    }

    private static boolean IsNodeRefProperty(String propId) {
        return nodeRefProperties.contains(propId);
    }

    private static boolean IsDateProperty(String prop) {
        return dateProperties.contains(prop);
    }

    private static boolean IsIntProperty(String prop) {
        return integerProperties.contains(prop);
    }

    protected void assertCanModify(String id) {
        boolean isAssignee = false;
        boolean isOwner = false;
        boolean isInitiator = false;

        // The value will be either user name or node reference
        String currentUser = getCurrentUser();
        if (this.isAdmin(currentUser)) {
            return;
        }

        String currentUserNodeRef = getCurrentUserNodeRef().toString();
        WorkflowTask task = this.workflowService.getTaskById(id);
        if (task == null) {
            logger.warn("Task with ID: '" + id + "' not found!");
            return;
        }

        Map<QName, Serializable> properties = task.getProperties();
        if (properties == null || properties.isEmpty()) {
            throw new Error("Cannot update task. No information available about this task!");
        }

        if (task.getProperties().containsKey(WorkflowModel.ASSOC_ASSIGNEE)) {
            Serializable assignee = properties.get(WorkflowModel.ASSOC_ASSIGNEE);
            if (assignee != null) {
                String assigneeNodeRef = assignee.toString();
                isAssignee = currentUserNodeRef.equals(assigneeNodeRef) || currentUser.equals(assigneeNodeRef);
            } else {
                logger.debug("No information available in task with ID '" + id
                        + "' for QName " + WorkflowModel.ASSOC_ASSIGNEE);
            }
        }

        if (task.getProperties().containsKey(ownerQName)) {
            Serializable owner = properties.get(ownerQName);
            if (owner != null) {
                String ownerNodeRef = owner.toString();
                isOwner = currentUserNodeRef.equals(ownerNodeRef) || currentUser.equals(ownerNodeRef);
            } else {
                logger.debug("No information available in task with ID '" + id
                        + "' for QName " + ownerQName);
            }
        }

        if (task.getProperties().containsKey(initiatorQName)) {
            Serializable initiator = properties.get(initiatorQName);
            if (initiator != null) {
                String initiatorNodeRef = initiator.toString();
                isInitiator = currentUserNodeRef.equals(initiatorNodeRef) || currentUser.equals(initiatorNodeRef);
            } else {
                logger.debug("No information available in task with ID '" + id
                        + "' for QName " + initiatorQName);
            }
        }

        if (!(isAssignee || isOwner || isInitiator)) {
            throw new Error("Cannot update task. The user is neither owner, initiator or assignee!");
        }
    }

    protected ArrayList<WorkflowTransition> getTaskTransitions(WorkflowTask task) {
        WorkflowTaskDefinition tD = task.getDefinition();
        QName reviewOutcomePropertyQName = (QName) task.getProperties().get(REVIEW_OUTCOME_PROPERTY);
        logger.debug("reviewOutcomePropertyQName is '" + reviewOutcomePropertyQName + "'");

        if (tD.getMetadata().getProperties().containsKey(reviewOutcomePropertyQName)) {
            ArrayList<WorkflowTransition> ret = new ArrayList<>();
            PropertyDefinition outcomePropertyDefinition = tD.getMetadata().getProperties()
                    .get(reviewOutcomePropertyQName);
            String shortOutcomeQName = reviewOutcomePropertyQName.toPrefixString(namespaceService).replace(":", "_");
            logger.debug("outcomePropertyDefinition is '" + outcomePropertyDefinition + "'");
            logger.debug("shortOutcomeQName is '" + shortOutcomeQName + "'");
            for (ConstraintDefinition constraintDefinition : outcomePropertyDefinition.getConstraints()) {
                Map<String, Object> parameters = constraintDefinition.getConstraint().getParameters();
                if (!parameters.containsKey(ALLOWED_VALUES)) {
                    logger.debug("The parameters of constraintDefinition do not contain ALLOWED_VALUES");
                    continue;
                }

                List<String> allowedValues = (List<String>) parameters.get(ALLOWED_VALUES);
                for (String allowedTransitionKey : allowedValues) {
                    logger.debug("Found allowed value '" + allowedTransitionKey + "'");
                    WorkflowTransition transition = createWorkflowTransition(shortOutcomeQName, allowedTransitionKey);
                    ret.add(transition);
                }
            }

            if (!ret.isEmpty()) {
                logger.debug("Found transitions in ConstraintDefinition");
                return ret;
            }
        }

        logger.debug("No transitions in ConstraintDefinition");
        return tD.getNode() != null
                ? new ArrayList(Arrays.asList(tD.getNode().getTransitions()))
                : new ArrayList<>();
    }

    private WorkflowTransition createWorkflowTransition(String shortOutcomeQName, String allowedTransitionKey) {
        logger.debug("Creating workflow transition" +
                " with shortOutcomeQName '" + shortOutcomeQName
                + "' and allowedTransitionKey '" + allowedTransitionKey + "'");
        String listConstraintTranslationKey =
                LIST_CONSTRAINT + "." + shortOutcomeQName + OPTIONS_LABEL + "." + allowedTransitionKey;
        logger.debug("listConstraintTranslationKey is '" + listConstraintTranslationKey + "'");
        String title = messageService.getMessage(listConstraintTranslationKey);
        logger.debug("title is '" + title + "'");
        return new WorkflowTransition(allowedTransitionKey, title, title, false);
    }

    protected Map<QName, Serializable> filterEditableProperties(WorkflowOrTaskChanges changes) {
        if (null == changes || null == changes.getPropertiesToSet()) {
            return new HashMap<>();
        }

        Map<QName, Serializable> editableProperties = new HashMap<>();
        for (String property : changes.getPropertiesToSet().keySet()) {
            if (uneditableProps.contains(property)) {
                logger.error("Trying to edit property that is not editable: '"
                        + property + "'. This property will be skipped");
                continue;
            }

            String normalizedPropertyName = ApixToAlfrescoNames.containsKey(property) ?
                    ApixToAlfrescoNames.get(property) : property;
            String valueToNormalize = changes.getPropertiesToSet().get(property);
            Serializable normalizedValue = this.normalizePropertyValueToAlfresco(property, valueToNormalize);
            editableProperties
                    .put(this.c.alfresco(new eu.xenit.apix.data.QName(normalizedPropertyName)), normalizedValue);
        }
        return editableProperties;
    }

    private Serializable normalizePropertyValueToAlfresco(String propId, String value) {
        if (IsIntProperty(propId)) {
            return Integer.parseInt(value);
        } else if (IsDateProperty(propId)) {
            return ISO8601DateFormat.parse(value);
        } else {
            return (IsNodeRefProperty(propId) ? this.c.alfresco(new eu.xenit.apix.data.NodeRef(value)) : value);
        }
    }

    protected Map<String, Serializable> toApixProperties(Map<QName, Serializable> props) {
        HashMap<String, Serializable> ret = new HashMap<>();

        for (QName property : props.keySet()) {
            if (property == null) {
                logger.error("Trying to convert properties with a null key.");
            } else {
                ret.put(property.toString(), toApixPropertyValue(props.get(property)));
            }
        }

        return ret;
    }

    private Serializable toApixPropertyValue(Serializable value) {
        if (value instanceof NodeRef) {
            NodeRef nodeRef = (NodeRef) value;
            return c.apix(nodeRef);
        }
        if (value instanceof List) {
            ArrayList<Serializable> list = new ArrayList<>((List) value);
            ArrayList<Serializable> result = new ArrayList<>();
            for (Serializable item : list) {
                result.add(toApixPropertyValue(item));
            }
            return result;
        }
        return value;
    }
}
