package eu.xenit.alfred.api.rest.staging.workflow;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.model.Task;
import eu.xenit.alfred.api.workflow.model.Workflow;
import eu.xenit.alfred.api.workflow.model.WorkflowDefinition;
import eu.xenit.alfred.api.workflow.model.WorkflowDefinitionList;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.alfred.api.workflow.search.TaskSearchQuery;
import eu.xenit.alfred.api.workflow.search.WorkflowSearchQuery;
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
public class WorkflowWebscript {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowWebscript.class);

    private final IWorkflowService workflowService;

    public WorkflowWebscript(@Qualifier("eu.xenit.alfred.api.workflow.IWorkflowService") IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(
            value = "/staging/workflows/definitions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WorkflowDefinitionList> getWorkflowDefinitions(
            @RequestParam(required = false) String[] exclude) {
        List<WorkflowDefinition> definitions = workflowService.getAllDefinitions();

        if (exclude != null && exclude.length > 0) {
            HashSet<String> excludeSet = new HashSet<>(Arrays.asList(exclude));
            definitions.removeIf(workflowDefinition -> excludeSet.contains(workflowDefinition.name));
        }

        return responseFrom(new WorkflowDefinitionList(definitions));
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/workflows/definition/{name}")
    public ResponseEntity<WorkflowDefinition> getWorkflowDefinition(@PathVariable final String name) {
        return responseFrom(workflowService.getWorkflowDefinition(name));
    }

    @AlfrescoTransaction
    @PostMapping(value = "/workflows/search")
    public ResponseEntity<TaskOrWorkflowSearchResult> workflowsActiviti(@RequestBody final WorkflowSearchQuery query) {
        TaskOrWorkflowSearchResult result = workflowService.searchWorkflows(query);
        logger.debug("Found results for workflows, # of results: {}", result.results.size());
        result.getFacets().CheckValidity();
        return responseFrom(result);
    }

    @AlfrescoTransaction
    @PostMapping(value = "/tasks/search")
    public ResponseEntity<TaskOrWorkflowSearchResult> tasksActiviti(@RequestBody final TaskSearchQuery query) {
        return responseFrom(workflowService.searchTasks(query));
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/workflows/{id}")
    public ResponseEntity<Workflow> workflow(@PathVariable final String id) {
        return responseFrom(workflowService.getWorkflowInfo(id));
    }

    @AlfrescoTransaction
    @PostMapping(value = "/workflows/{id}/start")
    public ResponseEntity<Workflow> startWorkflow(@PathVariable final String id,
            @RequestBody final Map<String, Serializable> variables) {
        logger.debug("variables: {}", variables);
        return responseFrom(workflowService.startWorkflow(id, variables));
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/tasks/{id}")
    public ResponseEntity<Void> task(@PathVariable final String id) {
        responseFrom(workflowService.getTaskInfo(id));
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @PutMapping(value = "/workflows/{id}")
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable final String id,
            @RequestBody final WorkflowOrTaskChanges changes) {
        return responseFrom(workflowService.updateWorkflow(id, changes));
    }

    @AlfrescoTransaction
    @DeleteMapping(value = "/workflows/{id}")
    public ResponseEntity<Void> cancelWorkflow(@PathVariable final String id) {
        workflowService.cancelWorkflow(id);
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @PutMapping(value = "/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable final String id,
            @RequestBody final WorkflowOrTaskChanges changes) {
        try {
            return responseFrom(workflowService.updateTask(id, changes));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SC_CONFLICT).build();
        }
    }

    @AlfrescoTransaction
    @PostMapping(value = "/staging/tasks/claim")
    public ResponseEntity<Task> claimTask(@RequestBody final WorkflowClaimsBody workflowClaimsBody) {
        Task wfTask;
        if (workflowClaimsBody.getUserName() != null) {
            logger.debug("Setting owner of task with id {} to {}", workflowClaimsBody.getId(),
                    workflowClaimsBody.getUserName());
            wfTask = workflowService.claimWorkflowTask(workflowClaimsBody.getId(), workflowClaimsBody.getUserName());
        } else {
            logger.debug("Setting owner of task with id ");
            wfTask = workflowService.claimWorkflowTask(workflowClaimsBody.getId());
        }
        return responseFrom(wfTask);
    }

    @AlfrescoTransaction
    @PostMapping(value = "/staging/tasks/release")
    public ResponseEntity<Task> releaseTask(@RequestBody final WorkflowReleaseBody workflowReleaseBody) {
        logger.debug("Setting owner of task {}", workflowReleaseBody);
        Task wfTask = workflowService.releaseWorkflowTask(workflowReleaseBody.getId());
        return responseFrom(wfTask);
    }

    @AlfrescoTransaction
    @PostMapping(value = "/staging/tasks/{id}/end/{transition}")
    public void transitionTask(@PathVariable String id, @PathVariable String transition) {
        workflowService.endTask(id, transition);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/staging/workflows/generate/{amount}/{username}")
    public void generateWorkflow(@PathVariable final int amount, @PathVariable final String username) {
        workflowService.GenerateWorkflows(amount, username);
    }

    protected <T> ResponseEntity<T> responseFrom(T object) {
        return ResponseEntity.ok(object);
    }
}
