package eu.xenit.apix.rest.staging.workflow;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowWebscript {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowWebscript.class);

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

    private final IWorkflowService workflowService;

    public WorkflowWebscript(@Qualifier("eu.xenit.apix.workflow.IWorkflowService") IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping(
            value = "/staging/workflows/definitions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(value = "Retrieve the definitions for all defined workflows")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = WorkflowDefinitionList.class))
    public ResponseEntity<WorkflowDefinitionList> getWorkflowDefinitions(@RequestParam(required = false)
            @ApiParam(value = "Comma separated definition names to exclude.") String[] exclude) {
        List<WorkflowDefinition> definitions = workflowService.getAllDefinitions();

        if (exclude != null && exclude.length > 0) {
            HashSet<String> excludeSet = new HashSet<>(Arrays.asList(exclude));
            definitions.removeIf(workflowDefinition -> excludeSet.contains(workflowDefinition.name));
        }

        return responseFrom(new WorkflowDefinitionList(definitions));
    }

    @GetMapping(value = "/workflows/definition/{name}")
    @ApiOperation(value = "Retrieve the definition for the specified workflow name")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = WorkflowDefinition.class))
    public ResponseEntity<WorkflowDefinition> getWorkflowDefinition(@PathVariable final String name) {
        return responseFrom(workflowService.getWorkflowDefinition(name));
    }

    @PostMapping(value = "/workflows/search")
    @ApiOperation(value = "Returns a collection of workflow instances",
            notes = "The result collection of workflow instances is sorted and filtered as requested in the provided" +
                    " WorkflowSearchQuery\nWorkflowSearchQuery Sample:\n" + WorkflowSearchQueryDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.WorkflowSearchQuery",
            paramType = "body",
            name = "body")})
    public ResponseEntity<TaskOrWorkflowSearchResult> workflowsActiviti(@RequestBody final WorkflowSearchQuery query) {
        TaskOrWorkflowSearchResult result = workflowService.searchWorkflows(query);
        logger.debug("Found results for workflows, # of results: {}", result.results.size());
        result.getFacets().CheckValidity();
        return responseFrom(result);
    }

    @PostMapping(value = "/tasks/search")
    @ApiOperation(value = "Returns a collection of workflow tasks",
            notes = "The result collection of workflow tasks is sorted and filtered as requested in the provided" +
                    " TaskSearchQuery\nTaskSearchQuery Sample:\n" + TaskSearchQueryDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.TaskSearchQuery",
            paramType = "body",
            name = "body")})
    public ResponseEntity<TaskOrWorkflowSearchResult> tasksActiviti(@RequestBody final TaskSearchQuery query) {
        return responseFrom(workflowService.searchTasks(query));
    }

    @GetMapping(value = "/workflows/{id}")
    @ApiOperation(value = "Retrieves a workflow with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    public ResponseEntity<Workflow> workflow(@PathVariable final String id) {
        return responseFrom(workflowService.getWorkflowInfo(id));
    }

    @PostMapping(value = "/workflows/{id}/start")
    public ResponseEntity<Workflow> startWorkflow(@PathVariable final String id,
                                                  @RequestBody final Map<String, Serializable> variables) {
        logger.debug("variables: {}", variables);
        return responseFrom(workflowService.startWorkflow(id, variables));
    }

    @GetMapping(value = "/tasks/{id}")
    @ApiOperation(value = "Retrieves a workflow task with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    public ResponseEntity<Void> task(@PathVariable final String id) {
        responseFrom(workflowService.getTaskInfo(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/workflows/{id}")
    @ApiOperation(value = "[Deprecated] Updates a workflow with the provided id, with the provided information from" +
            " the provided WorkflowChanges\nWorkflowChanges Sample:\n"
            + WorkflowChangesOrTaskChangesDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Workflow.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.model.WorkflowChanges",
            paramType = "body",
            name = "body")})
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable final String id,
                                                    @RequestBody final WorkflowOrTaskChanges changes) {
        return responseFrom(workflowService.updateWorkflow(id, changes));
    }

    @DeleteMapping(value = "/workflows/{id}")
    @ApiOperation(value = "Cancels a workflow with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public ResponseEntity<Void> cancelWorkflow(@PathVariable final String id) {
        workflowService.cancelWorkflow(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/tasks/{id}")
    @ApiOperation(value = "Updates a workflow task with the provided id, with the provided information from" +
            " the provided TaskChanges\nTaskChanges Sample:\n" + WorkflowChangesOrTaskChangesDocumentationSample)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Task.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "eu.xenit.apix.workflow.model.TaskChanges",
            paramType = "body",
            name = "body")})
    public ResponseEntity<Task> updateTask(@PathVariable  final String id,
                                            @RequestBody final WorkflowOrTaskChanges changes) {
        try {
            return responseFrom(workflowService.updateTask(id, changes));
        } catch (Error ex) {
            return ResponseEntity.status(HttpStatus.SC_CONFLICT).build();
            // TODO @Zlatin Alfresco MVC
//            responseFrom(webScriptResponse, ex);
        }
    }

    @PostMapping(value = "/staging/tasks/claim")
    @ApiOperation(value = "Claims the task with the provided id for a user",
            notes = "The user parameter is optional. If not provided, Alfred API will default to the current user")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "String",
            paramType = "body",
            name = "body")})
    public ResponseEntity<Task> claimTask(@RequestBody final Map<String, String> body) {
        logger.debug("Input: {}", body);
        String id = body.get("id");
        String userName = body.get("userName");

        Task wfTask;
        if (userName != null) {
            logger.debug("Setting owner of task with id {} to {}", id, userName);
            wfTask = workflowService.claimWorkflowTask(id, userName);
        } else {
            logger.debug("Setting owner of task with id ");
            wfTask = workflowService.claimWorkflowTask(id);
        }
        return responseFrom(wfTask);
    }

    @PostMapping(value = "/staging/tasks/release")
    @ApiOperation(value = "Releases the task with the provided id")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class))
    @ApiImplicitParams({@ApiImplicitParam(
            dataType = "String",
            paramType = "body",
            name = "body")})
    public ResponseEntity<Task> releaseTask(@RequestBody final Map<String, String> body) {
        logger.debug("Setting owner of task {}", body);
        String id = body.get("id");
        Task wfTask = workflowService.releaseWorkflowTask(id);
        return responseFrom(wfTask);
    }

    @PostMapping(value = "/staging/tasks/{id}/end/{transition}")
    @ApiOperation(value = "Ends the workflow task with the provided id with the provided transition as next")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void transitionTask(@PathVariable String id, @PathVariable String transition) {
        workflowService.endTask(id, transition);
    }

    @GetMapping(value = "/staging/workflows/generate/{amount}/{username}")
    @ApiOperation(value = "[DEV] Generate an amount of workflows for username")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public void generateWorkflow(@PathVariable final int amount, @PathVariable final String username) {
        workflowService.GenerateWorkflows(amount, username);
    }

    protected <T> ResponseEntity<T> responseFrom(T object) {
        return ResponseEntity.ok(object);
    }
}
