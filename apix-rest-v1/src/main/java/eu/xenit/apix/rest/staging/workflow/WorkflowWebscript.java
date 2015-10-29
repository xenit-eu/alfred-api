package eu.xenit.apix.rest.staging.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.staging.ApixStagingWebscript;
import eu.xenit.apix.rest.staging.RestStagingConfig;
import eu.xenit.apix.rest.v1.ExceptionObject;
import eu.xenit.apix.search.SearchQueryResult;
import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.model.Task;
import eu.xenit.apix.workflow.model.Workflow;
import eu.xenit.apix.workflow.model.WorkflowDefinition;
import eu.xenit.apix.workflow.model.WorkflowDefinitionList;
import eu.xenit.apix.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.apix.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.apix.workflow.search.TaskSearchQuery;
import eu.xenit.apix.workflow.search.WorkflowSearchQuery;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@WebScript(baseUri = RestStagingConfig.BaseUrl, families = RestStagingConfig.Family, defaultFormat = "json",
        description = "Perform workflow and task operations", value = "Workflows")
@Authentication(AuthenticationType.USER)
@Qualifier("eu.xenit.apix.rest.staging.workflow.WorkflowWebscript")
@Component("eu.xenit.apix.rest.staging.workflow.WorkflowWebscript")
public class WorkflowWebscript extends ApixStagingWebscript {

    public static final ISO8601DateFormat DATE_FORMAT = new ISO8601DateFormat();

    ///<editor-fold desc="Documentation Samples for provided JSON parameters">
    private static final String WorkflowSearchQueryDocumentationSample = "{\n" +
            "\t\"filters\": [],\n" +
            "\t\"facets\": [\n" +
            "\t\t\"{http://www.alfresco.org/model/content/1.0}owner\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}dueDate\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}startDate\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}endDate\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}workflowPriority\"\n" +
            "\t],\n" +
            "\t\"scope\": \"WorkflowsIveStarted\",\n" +
            "\t\"paging\": {\n" +
            "\t\t\"skip\": 0,\n" +
            "\t\t\"limit\": 15\n" +
            "\t}\n" +
            "}";
    private static final String TaskSearchQueryDocumentationSample = "{\n" +
            "\t\"facets\": [\n" +
            "\t\t\"{http://www.alfresco.org/model/content/1.0}owner\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}priority\"\n" +
            "\t],\n" +
            "\t\"filters\": [],\n" +
            "\t\"scope\": \"MyTasks\"|\"MyPooledTasks\"|\"AllTasks\",\n" +
            "\t\"paging\": {\n" +
            "\t\t\"skip\": 0,\n" +
            "\t\t\"limit\": 15\n" +
            "\t}\n" +
            "}";
    private static final String WorkflowChangesOrTaskChangesDocumentationSample = "{\n" +
            "\t\"propertiesToSet\": {\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}priority\": \"1\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}status\": \"On Hold\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}startDate\": \"2018-01-29T23:00:00.000Z\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}title\": \"Task 1\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}dueDate\": \"2018-01-24T23:00:00.000Z\",\n" +
            "\t\t\"{http://www.alfresco.org/model/bpm/1.0}description\": \"Task allocated by colleague 2\"\n" +
            "\t}\n" +
            "}";
    ///</editor-fold>

    @Qualifier("eu.xenit.apix.workflow.IWorkflowService")
    @Autowired
    IWorkflowService workflowService;

    Logger logger = LoggerFactory.getLogger(WorkflowWebscript.class);

    @Uri(value = "/workflows/definitions", method = HttpMethod.GET, defaultFormat = "json")
    @ApiOperation(value = "Retrieve the definitions for all defined workflows")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = WorkflowDefinitionList.class))
    public void getWorkflowDefinitions(@RequestParam(delimiter = ",", required = false)
            @ApiParam(value = "Comma separated definition names to exclude.") String[] exclude,
            WebScriptResponse response) throws IOException {
        List<WorkflowDefinition> definitions = workflowService.getAllDefinitions();

        if (exclude != null && exclude.length > 0) {
            HashSet<String> excludeSet = new HashSet<>(Arrays.asList(exclude));
            ListIterator<WorkflowDefinition> iter = definitions.listIterator();
            while (iter.hasNext()) {
                if (excludeSet.contains(iter.next().name)) {
                    iter.remove();
                }
            }
        }

        writeJsonResponse(response, new WorkflowDefinitionList(definitions));
    }

    @Uri(value = "/workflows/definition/{name}", method = HttpMethod.GET)
    @ApiOperation(value = "Retrieve the definition for the specified workflow name")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = WorkflowDefinition.class))
    public void getWorkflowDefinition(@UriVariable final String name, WebScriptResponse response) throws IOException {
        WorkflowDefinition definition = workflowService.getWorkflowDefinition(name);
        writeJsonResponse(response, definition);
    }

    @Uri(value = "/workflows/search", method = HttpMethod.POST)
    @ApiOperation(value = "Returns a collection of workflow instances",
            notes = "The result collection of workflow instances is sorted and filtered as requested in the provided" +
                    " WorkflowSearchQuery\nWorkflowSearchQuery Sample:\n" + WorkflowSearchQueryDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.WorkflowSearchQuery",
            paramType = "body",
            name = "body")})
    public void workflowsActiviti(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse)
            throws IOException {
        ObjectMapper m = new WorkflowJsonParser().getObjectMapper();
        m.setDateFormat(DATE_FORMAT);
        InputStream stream = webScriptRequest.getContent().getInputStream();
        WorkflowSearchQuery q = m.readValue(stream, WorkflowSearchQuery.class);
        TaskOrWorkflowSearchResult result = workflowService.searchWorkflows(q);
        logger.debug("Found results for workflows");
        logger.debug("Nb of results: " + result.results.size());
        result.getFacets().CheckValidity();
        writeJsonResponse(webScriptResponse, result);
    }

    @Uri(value = "/tasks/search", method = HttpMethod.POST)
    @ApiOperation(value = "Returns a collection of workflow tasks",
            notes = "The result collection of workflow tasks is sorted and filtered as requested in the provided" +
                    " TaskSearchQuery\nTaskSearchQuery Sample:\n" + TaskSearchQueryDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.TaskSearchQuery",
            paramType = "body",
            name = "body")})
    public void tasksActiviti(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse)
            throws IOException {
        ObjectMapper m = new WorkflowJsonParser().getObjectMapper();
        InputStream stream = webScriptRequest.getContent().getInputStream();
        TaskSearchQuery q = m.readValue(stream, TaskSearchQuery.class);
        TaskOrWorkflowSearchResult result = workflowService.searchTasks(q);
        writeJsonResponse(webScriptResponse, result);
    }

    @Uri(value = "/workflows/{id}", method = HttpMethod.GET)
    @ApiOperation(value = "Retrieves a workflow with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    public void workflow(@UriVariable final String id, WebScriptResponse response) throws IOException {
        writeJsonResponse(response, workflowService.getWorkflowInfo(id));
    }

    @Uri(value = "/workflows/{id}/start", method = HttpMethod.POST)
    public void startWorkflow(@UriVariable final String id, @RequestBody Map<String, Serializable> variables,
            WebScriptResponse response) throws IOException {
        for (Map.Entry<String, Serializable> e : variables.entrySet()) {
            logger.debug("{}: {}", e.getKey(), e.getValue());
        }

        Workflow workflow = workflowService.startWorkflow(id, variables);
        writeJsonResponse(response, workflow);
    }

    @Uri(value = "/tasks/{id}", method = HttpMethod.GET)
    @ApiOperation(value = "Retrieves a workflow task with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    public void task(@UriVariable final String id, WebScriptResponse response) throws IOException {
        writeJsonResponse(response, workflowService.getTaskInfo(id));
    }

    @Uri(value = "/workflows/{id}", method = HttpMethod.PUT)
    @ApiOperation(value = "[Deprecated] Updates a workflow with the provided id, with the provided information from" +
            " the provided WorkflowChanges\nWorkflowChanges Sample:\n"
            + WorkflowChangesOrTaskChangesDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Workflow.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.model.WorkflowChanges",
            paramType = "body",
            name = "body")})
    public void updateWorkflow(
            @UriVariable String id,
            WorkflowOrTaskChanges changes,
            WebScriptRequest webScriptRequest,
            WebScriptResponse webScriptResponse) throws IOException {
        Workflow ret = workflowService.updateWorkflow(id, changes);
        writeJsonResponse(webScriptResponse, ret);
    }

    @Uri(value = "/workflows/{id}", method = HttpMethod.DELETE)
    @ApiOperation(value = "Cancels a workflow with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void cancelWorkflow(
            @UriVariable String id,
            WebScriptRequest webScriptRequest,
            WebScriptResponse webScriptResponse) throws IOException {
        workflowService.cancelWorkflow(id);
    }

    @Uri(value = "/tasks/{id}", method = HttpMethod.PUT)
    @ApiOperation(value = "Updates a workflow task with the provided id, with the provided information from" +
            " the provided TaskChanges\nTaskChanges Sample:\n" + WorkflowChangesOrTaskChangesDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.model.TaskChanges",
            paramType = "body",
            name = "body")})
    public void updateTask(
            @UriVariable String id,
            WorkflowOrTaskChanges changes,
            WebScriptRequest webScriptRequest,
            WebScriptResponse webScriptResponse
    ) throws IOException {
        try {
            Task ret = workflowService.updateTask(id, changes);
            writeJsonResponse(webScriptResponse, ret);
        } catch (Error ex) {
            webScriptResponse.setStatus(HttpStatus.SC_CONFLICT);
            writeJsonResponse(webScriptResponse, new ExceptionObject(ex));
        }
    }

    @Uri(value = "/tasks/claim", method = HttpMethod.POST)
    @ApiOperation(value = "Claims the task with the provided id for a user",
            notes = "The user parameter is optional. If not provided, Alfred API will default to the current user")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "String",
            paramType = "body",
            name = "body")})
    public void claimTask(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        ObjectMapper m = new WorkflowJsonParser().getObjectMapper();
        JsonNode input = m.readTree(webScriptRequest.getContent().getContent());
        logger.debug("Input: " + input);
        JsonNode id = input.get("id");
        JsonNode userName = input.get("userName");

        Task wfTask;
        if (userName != null) {
            logger.debug("Setting owner of task with id " + id.asText() + " to " + userName);
            wfTask = workflowService.claimWorkflowTask(id.asText(), userName.asText());
        } else {
            logger.debug("Setting owner of task with id ");
            wfTask = workflowService.claimWorkflowTask(id.asText());
        }
        writeJsonResponse(webScriptResponse, wfTask);
    }

    @Uri(value = "/tasks/release", method = HttpMethod.POST)
    @ApiOperation(value = "Releases the task with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "String",
            paramType = "body",
            name = "body")})
    public void releaseTask(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse)
            throws IOException {
        ObjectMapper m = new WorkflowJsonParser().getObjectMapper();
        JsonNode input = m.readTree(webScriptRequest.getContent().getContent());
        logger.debug("Input: " + input);
        JsonNode id = input.get("id");

        logger.debug("Setting owner of task with id " + id.asText());
        Task wfTask = workflowService.releaseWorkflowTask(id.asText());
        writeJsonResponse(webScriptResponse, wfTask);
    }

    @Uri(value = "/tasks/{id}/end/{transition}", method = HttpMethod.POST)
    @ApiOperation(value = "Ends the workflow task with the provided id with the provided transition as next")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void transitionTask(@UriVariable String id, @UriVariable String transition) {
        workflowService.endTask(id, transition);
    }

    @Uri(value = "/workflows/generate/{amount}/{username}", method = HttpMethod.GET)
    @ApiOperation(value = "[DEV] Generate an amount of workflows for username")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void generateWorkflow(@UriVariable final int amount, @UriVariable final String username) {
        workflowService.GenerateWorkflows(amount, username);
    }

    //Include correct date format
    protected void writeJsonResponse(WebScriptResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setContentEncoding("utf-8");
        response.setHeader("Cache-Control", "no-cache");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new ISO8601DateFormat());
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(response.getWriter(), object);
    }
}
