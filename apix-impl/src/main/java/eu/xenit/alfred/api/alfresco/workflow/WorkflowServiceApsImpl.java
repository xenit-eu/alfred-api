package eu.xenit.alfred.api.alfresco.workflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.xenit.alfred.api.alfresco.ApixSpringConfiguration;
import eu.xenit.alfred.api.alfresco.workflow.aps.ApsFormDefinition;
import eu.xenit.alfred.api.alfresco.workflow.aps.ApsFormField;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.model.Task;
import eu.xenit.alfred.api.workflow.model.Workflow;
import eu.xenit.alfred.api.workflow.model.WorkflowDefinition;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.alfred.api.workflow.search.TaskSearchQuery;
import eu.xenit.alfred.api.workflow.search.WorkflowSearchQuery;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceApsImpl implements IWorkflowService {

    @Autowired
    private ApixSpringConfiguration configuration;

    private static final String APS_REST_DEFAULT_BASE_URL = "http://process-service:8080/activiti-app/api";
    private static final String APS_REST_DEFAULT_USERNAME = "admin";
    private static final String APS_REST_DEFAULT_PASSWORD = "admin";
    private final Logger logger = LoggerFactory.getLogger(WorkflowServiceApsImpl.class);

    private HttpEntity doHttp(HttpUriRequest request) {
        HttpClient httpClient = new DefaultHttpClient();
        logger.debug("Doing HTTP {} to {}", request.getMethod(), request.getURI());
        try {
            request.setHeader("Authorization", getApsAuthorization());
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 300 || statusCode < 200) {
                String errorMessage = String.format("Apix HttpClient encountered status \"%d: %s\" during %s %s",
                        statusCode, response.getStatusLine().getReasonPhrase(), request.getMethod(), request.getURI());
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            return response.getEntity();
        } catch (IOException e) {
            String errorMessage = String.format("Apix HttpClient encountered IOException during %s %s",
                    request.getMethod(), request.getURI());
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public List<WorkflowDefinition> getAllDefinitions() {
        boolean onlyLatest = false; // TODO: parse from URL param
        return getAllDefinitions(onlyLatest);
    }

    private List<WorkflowDefinition> getAllDefinitions(boolean onlyLatest) {
        String targetUrl = getApsRestBaseUrl() + "/enterprise/process-definitions" + (onlyLatest ? "?latest=true" : "");
        logger.debug("Getting workflow definitions from {}", targetUrl);
        HttpGet get = new HttpGet(targetUrl);
        ObjectMapper mapper = new ObjectMapper();
        HttpEntity resultEntity = doHttp(get);

        List<WorkflowDefinition> list = new ArrayList<>();
        JsonNode result;
        try {
            result = mapper.readTree(resultEntity.getContent());
        } catch (IOException e) {
            throw new RuntimeException("WorkflowServiceApsImpl encountered IOException in getAllDefinitions()", e);
        }
        for (JsonNode node : result.get("data")) {
            WorkflowDefinition def = new WorkflowDefinition();
            def.id = node.get("id").asText();
            def.key = node.get("key").asText();
            def.name = node.get("name").asText();
            def.title = node.get("name").asText();
            def.description = node.get("description").asText();
            def.version = node.get("version").asText();
            list.add(def);
        }

        return list;

    }

    @Override
    public TaskOrWorkflowSearchResult searchWorkflows(WorkflowSearchQuery workflowSearchQuery) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public TaskOrWorkflowSearchResult searchTasks(TaskSearchQuery taskSearchQuery) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Task getTaskInfo(String taskID) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Workflow getWorkflowInfo(String workflowID) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");

    }

    @Override
    public void endTask(String taskID, String transitionID) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Workflow updateWorkflow(String id, WorkflowOrTaskChanges changes) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Task updateTask(String id, WorkflowOrTaskChanges changes) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Task claimWorkflowTask(String taskID) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Task claimWorkflowTask(String taskID, String userName) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public Task releaseWorkflowTask(String taskID) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public void GenerateWorkflows(int amount, String username) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public void cancelWorkflow(String id) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    @Override
    public WorkflowDefinition getWorkflowDefinition(String workflowName) {
        throw new org.apache.commons.lang3.NotImplementedException(
                "Focus is on the creation of an APS implementation of the workflow service."
                        + " Will return later if possible/when necessary.");
    }

    public Map<QName, Serializable> getStartingParameters(String definitionId) {
        Map<QName, Serializable> response = new HashMap<>();
        String targetUrl = getApsRestBaseUrl() + "/process-definitions/" + definitionId + "/start-form";
        ObjectMapper mapper = new ObjectMapper();
        HttpPost postDirective = new HttpPost(targetUrl);
        HttpEntity entity = doHttp(postDirective);

        try {
            ApsFormDefinition startForm = mapper.readValue(entity.getContent(), ApsFormDefinition.class);
            for (Entry<String, ApsFormField> field : startForm.allFieldsAsMap().entrySet()) {
                QName fieldName = new QName(field.getKey());
                Serializable fieldType = field.getValue().getType();
                response.put(fieldName, fieldType);
            }
        } catch (IOException e) {
            //TODO Consider retrying & transactional behaviour
            logger.error("WorkflowServiceApsImpl encountered IOException in getStartingParameters()");
            throw new RuntimeException("WorkflowServiceApsImpl encountered IOException in getStartingParameters()", e);
        }
        return response;
    }

    @Override
    public Workflow startWorkflow(String definitionKeyOrId, Map<String, Serializable> parameters) {
        //needs:
        //  - either processDefinitionId or processDefinitionKey, I'd advise the key since it is simpler
        //  - name of the process to start (optional)
        //  - either values and outcome, or variables → values is a json object with form field id to value
        //    pairs, outcome is the wanted form outcome; variables is a json array of  ?something- i suspect
        //    process variables?

        String definitionKey = getWorkflowDefinitionKey(definitionKeyOrId);

        String targetUrl = getApsRestBaseUrl() + "/enterprise/process-instances";
        logger.debug("Posting to {}", targetUrl);
        ObjectMapper mapper = new ObjectMapper();
        HttpPost postDirective = new HttpPost(targetUrl);

        JsonNode variables = mapper.valueToTree(ApsVarRepresentation.listFromMap(parameters));
        ObjectNode root = mapper.createObjectNode();
        root.put("processDefinitionKey", definitionKey);
        root.set("variables", variables);

        try {
            String jsonString = mapper.writeValueAsString(root);
            logger.debug("Post body:\n" + jsonString);
            StringEntity jsonBody = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            postDirective.setEntity(jsonBody);
            HttpEntity res = doHttp(postDirective);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(res.getContent(), Workflow.class);
        } catch (IOException e) {
            //TODO Consider retrying & transactional behaviour
            logger.error("WorkflowServiceApsImpl encountered IOException in startWorkflow()");
            throw new RuntimeException("WorkflowServiceApsImpl encountered IOException in startWorkflow()", e);
        }
    }

    private String getWorkflowDefinitionKey(String definitionKeyOrId) {
        List<WorkflowDefinition> definitions = getAllDefinitions(true);
        for (WorkflowDefinition def : definitions) {
            logger.debug("Found workflow definition with id='{}', key='{}', name='{}'", def.id, def.key, def.name);
            // Yes, a match on name suffices too. It's what some clients are used to and we don't break compatibility
            if (definitionKeyOrId.equals(def.key)
                    || definitionKeyOrId.equals(def.id)
                    || definitionKeyOrId.equals(def.name)) {
                return def.key;
            }
        }
        throw new RuntimeException("Could not find workflow '" + definitionKeyOrId + "' on APS");
    }

    /**
     * Helper class for representing an APS process variable, serializes nicely to the json that APS expects.
     */
    private static class ApsVarRepresentation {
        // Class names recognized by APS
        private final static Set<String> knownClasses = new HashSet<>(Arrays.asList(
                "string", "integer", "boolean", "double", "date"));

        public String name;
        public String type;
        public Serializable value;

        ApsVarRepresentation(String name, Serializable value) {
            this.name = name;
            this.value = value;
            String simpleType = value.getClass().getSimpleName().toLowerCase();
            this.type = knownClasses.contains(simpleType) ? simpleType : null;
        }

        static List<ApsVarRepresentation> listFromMap(Map<String, Serializable> map) {
            List<ApsVarRepresentation> list = new ArrayList<>();
            for (Map.Entry<String, Serializable> e : map.entrySet()) {
                list.add(new ApsVarRepresentation(e.getKey(), e.getValue()));
            }
            return list;
        }
    }

    private String getApsRestBaseUrl() {
        if (configuration == null || configuration.getProperties() == null) {
            return APS_REST_DEFAULT_BASE_URL;
        }
        return configuration.getProperties().getProperty("aps.rest.base_url", APS_REST_DEFAULT_BASE_URL);
    }
    private String getApsRestUsername() {
        if (configuration == null || configuration.getProperties() == null) {
            return APS_REST_DEFAULT_USERNAME;
        }
        return configuration.getProperties().getProperty("aps.rest.username", APS_REST_DEFAULT_USERNAME);
    }

    private String getApsRestPassword() {
        if (configuration == null || configuration.getProperties() == null) {
            return APS_REST_DEFAULT_PASSWORD;
        }
        return configuration.getProperties().getProperty("aps.rest.password", APS_REST_DEFAULT_PASSWORD);
    }

    private String getApsAuthorization() {
        byte[] bytes = (getApsRestUsername() + ":" + getApsRestPassword()).getBytes(Charset.forName("UTF-8"));
        return "Basic " + new String(Base64.encodeBase64(bytes));
    }
}
