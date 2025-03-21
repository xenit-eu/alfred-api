package eu.xenit.alfred.api.alfresco.workflow;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.alfresco.workflow.activiti.query.AlfredApiHistoricInstanceQuery;
import eu.xenit.alfred.api.alfresco.workflow.utils.DebugHelper;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.model.Task;
import eu.xenit.alfred.api.workflow.model.Workflow;
import eu.xenit.alfred.api.workflow.model.WorkflowDefinition;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.AuthorityFilter;
import eu.xenit.alfred.api.workflow.search.IQueryFilter;
import eu.xenit.alfred.api.workflow.search.SearchQuery;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import eu.xenit.alfred.api.workflow.search.TaskSearchQuery;
import eu.xenit.alfred.api.workflow.search.WorkflowSearchQuery;
import eu.xenit.alfred.api.workflow.search.WorkflowSearchQuery.QueryScope;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


public class WorkflowServiceActivitiImpl implements IWorkflowService, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(WorkflowServiceActivitiImpl.class);
    private AlfredApiToAlfrescoConversion c;
    private IPeopleService peopleService;
    private AbstractQueryConverterFactory activitiQueryConverterFactory;
    private AbstractAlfredApiQueryConverter alfredApiWfTaskQueryConverter;
    private AbstractAlfredApiQueryConverter alfredApiWfProcQueryConverter;
    private AuthenticationService authenticationService;
    private WorkflowService workflowService;

    public WorkflowServiceActivitiImpl(
            ServiceRegistry serviceRegistry,
            AlfredApiToAlfrescoConversion c,
            AbstractAlfredApiQueryConverter alfredApiWfProcQueryConverter,
            AbstractAlfredApiQueryConverter alfredApiWfTaskQueryConverter,
            IPeopleService peopleService,
            AbstractQueryConverterFactory activitiQueryConverterFactory) {
        this.c = c;
        this.peopleService = peopleService;
        this.activitiQueryConverterFactory = activitiQueryConverterFactory;
        this.alfredApiWfProcQueryConverter = alfredApiWfProcQueryConverter;
        this.alfredApiWfTaskQueryConverter = alfredApiWfTaskQueryConverter;
        authenticationService = serviceRegistry.getAuthenticationService();
        workflowService = serviceRegistry.getWorkflowService();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        this.alfredApiWfProcQueryConverter = this.activitiQueryConverterFactory.getProcessQueryConverter();
        this.alfredApiWfTaskQueryConverter = this.activitiQueryConverterFactory.getTasksQueryConverter();
    }

    public void GenerateWorkflows(int amount, String username) {
        this.alfredApiWfProcQueryConverter.generate(amount, username);
    }

    @Override
    public WorkflowDefinition getWorkflowDefinition(String workflowName) {
        org.alfresco.service.cmr.workflow.WorkflowDefinition alfDef = workflowService.getDefinitionByName(workflowName);
        if (alfDef == null) {
            return null;
        }
        return convertDefinition(alfDef);
    }

    @Override
    public List<WorkflowDefinition> getAllDefinitions() {
        List<WorkflowDefinition> list = new ArrayList<>();
        for (org.alfresco.service.cmr.workflow.WorkflowDefinition alfDef : workflowService.getAllDefinitions()) {
            list.add(convertDefinition(alfDef));
        }
        return list;
    }

    private WorkflowDefinition convertDefinition(org.alfresco.service.cmr.workflow.WorkflowDefinition alfDef) {
        WorkflowDefinition alfredApiDef = new WorkflowDefinition();

        alfredApiDef.id = alfDef.getId();
        alfredApiDef.key = alfDef.getId().substring(0, alfDef.getId().indexOf(':'));
        alfredApiDef.name = alfDef.getName();
        alfredApiDef.title = alfDef.getTitle();
        alfredApiDef.description = alfDef.getDescription();
        alfredApiDef.version = alfDef.getVersion();

        return alfredApiDef;
    }

    public void endTask(String taskID, String transitionID) {
        this.alfredApiWfTaskQueryConverter.end(taskID, transitionID);
    }

    public Task getTaskInfo(String taskID) {
        return (Task) this.alfredApiWfTaskQueryConverter.get(taskID);
    }

    public Workflow getWorkflowInfo(String workflowID) {
        return (Workflow) this.alfredApiWfProcQueryConverter.get(workflowID);
    }

    public Workflow updateWorkflow(String id, WorkflowOrTaskChanges changes) {
        this.alfredApiWfProcQueryConverter.update(id, changes);
        return this.getWorkflowInfo(id);
    }

    public Task updateTask(String id, WorkflowOrTaskChanges changes) {
        this.alfredApiWfTaskQueryConverter.update(id, changes);
        return this.getTaskInfo(id);
    }

    public Task claimWorkflowTask(String taskID) {
        this.alfredApiWfTaskQueryConverter.claim(taskID);
        return this.getTaskInfo(taskID);
    }

    public Task claimWorkflowTask(String taskID, String userName) {
        this.alfredApiWfTaskQueryConverter.claim(taskID, userName);
        return this.getTaskInfo(taskID);
    }

    public Task releaseWorkflowTask(String taskID) {
        this.alfredApiWfTaskQueryConverter.release(taskID);
        return this.getTaskInfo(taskID);
    }

    public TaskOrWorkflowSearchResult searchWorkflows(WorkflowSearchQuery searchQuery) {
        logger.debug("searchWorkflows Filters size: " + searchQuery.filters.size());
        if (searchQuery.scope == null) {
            searchQuery.scope = QueryScope.AllWorkflows;
        }
        searchQuery.restrictResultsToUser(this.authenticationService.getCurrentUserName());
        return this.fetch(searchQuery, this.alfredApiWfProcQueryConverter);
    }

    public TaskOrWorkflowSearchResult searchTasks(TaskSearchQuery searchQuery) {
        logger.debug("searchTasks Filters size: " + searchQuery.filters.size());
        if (searchQuery.scope == null) {
            searchQuery.scope = TaskSearchQuery.QueryScope.AllTasks;
        }
        searchQuery.restrictResultsToUser(this.authenticationService.getCurrentUserName());
        return this.fetch(searchQuery, this.alfredApiWfTaskQueryConverter);
    }

    private TaskOrWorkflowSearchResult fetch(TaskSearchQuery searchQuery,
            AbstractAlfredApiQueryConverter queryConverter) {
        logger.debug("fetch Filters size: " + searchQuery.filters.size());
        List result = TaskSearchQuery.QueryScope.MyPooledTasks == searchQuery.scope
                ? fetchMyPooledTasks(searchQuery, queryConverter)
                : fetchSingleQuery(searchQuery, queryConverter);
        return queryConverter.CreateSearchResult(result, searchQuery.includeResults, searchQuery.paging);
    }

    private TaskOrWorkflowSearchResult fetch(WorkflowSearchQuery searchQuery,
            AbstractAlfredApiQueryConverter queryConverter) {
        return queryConverter
                .CreateSearchResult(fetchSingleQuery(searchQuery, queryConverter), searchQuery.includeResults,
                        searchQuery.paging);
    }

    private <T> List<T> fetchMyPooledTasks(TaskSearchQuery searchQuery,
            AbstractAlfredApiQueryConverter queryConverter) {
        List<T> result = new ArrayList<>();

        List<IQueryFilter> otherFilters = new ArrayList<>();
        List<AuthorityFilter> authorityFilters = new ArrayList<>();

        logger.debug("fetchMyPooledTasks before filterAuthorities Filters size: " + searchQuery.filters.size());
        this.filterAuthorities(searchQuery, authorityFilters, otherFilters);
        logger.debug("fetchMyPooledTasks after filterAuthorities Filters size: " + searchQuery.filters.size());
        if (authorityFilters.size() == 0) {
            logger.debug("0 authority filters");
            return fetchSingleQuery(searchQuery, queryConverter);
        }

        for (AuthorityFilter authFilter : authorityFilters) {
            logger.debug("Filtering for user candidate: " + authFilter.getValue());
            boolean isUser = this.peopleService.isUser(authFilter.getValue());
            if (!(isUser || this.peopleService.isGroup(authFilter.getValue()))) {
                this.logger.warn("Unsupported authority type: " + authFilter.getValue());
                continue;
            }

            logger.debug("isUser: " + isUser);
            if (isUser) {
                List<String> groups = this.peopleService.GetPerson(authFilter.getValue()).getGroups();
                logger.debug("Groups is null: " + (groups == null));
                logger.debug("Groups size: " + groups.size());

                for (String group : groups) {
                    logger.debug("Group found: " + group);
                    AuthorityFilter groupFilter = new AuthorityFilter(group, authFilter.getProperty(),
                            AuthorityFilter.TYPE);
                    List<T> subResult = this.fetchSubQuery(groupFilter, otherFilters, searchQuery, queryConverter);
                    result.addAll(subResult);
                }
            }

            List<T> subResult = fetchSubQuery(authFilter, otherFilters, searchQuery, queryConverter);
            result.addAll(subResult);
        }

        return result;
    }

    private <T> List<T> fetchSubQuery(IQueryFilter authFilter, List<IQueryFilter> otherFilters,
            TaskSearchQuery searchQuery, AbstractAlfredApiQueryConverter queryConverter) {
        TaskSearchQuery userSubQuery = new TaskSearchQuery(searchQuery);
        userSubQuery.filters = new ArrayList<>(otherFilters);
        userSubQuery.filters.add(authFilter);
        return this.fetchSingleQuery(userSubQuery, queryConverter);
    }

    private void filterAuthorities(SearchQuery searchQuery, List<AuthorityFilter> authorityFilters,
            List<IQueryFilter> otherFilters) {
        for (IQueryFilter filter : searchQuery.filters) {
            if (filter.getType().equals(AuthorityFilter.TYPE)) {
                authorityFilters.add((AuthorityFilter) filter);
            } else {
                otherFilters.add(filter);
            }
        }

    }

    private <T> List<T> fetchSingleQuery(SearchQuery searchQuery, AbstractAlfredApiQueryConverter queryConverter) {
        List<T> result = new ArrayList<>();
        long start = System.nanoTime();
        AlfredApiHistoricInstanceQuery wq = queryConverter.convertQuery(searchQuery);
        this.PrintCurrentTimeElapsed("dateFilteredWorkflows in: ", start);
        List<T> queryResult = wq.getAll();
        this.PrintCurrentTimeElapsed("dateFilteredWorkflows in: ", start);
        result.addAll(queryResult);
        this.PrintCurrentTimeElapsed("modifiableTasksCollection created in: ", start);
        return result;
    }

    private void PrintCurrentTimeElapsed(String message, long start) {
        DebugHelper.PrintCurrentTimeElapsed(this.logger, message, start);
    }

    @Override
    public void cancelWorkflow(String id) {
        workflowService.cancelWorkflow(id);
    }

    @Override
    public Workflow startWorkflow(String definitionId, Map<String, Serializable> parameters) {
        Map<org.alfresco.service.namespace.QName, Serializable> convertedParams = new HashMap<>();

        for (Map.Entry<String, Serializable> entry : parameters.entrySet()) {
            Serializable value = entry.getValue();
            if (isNodeRef(value)) {
                value = new NodeRef((String) value);
            }
            convertedParams.put(c.alfresco(new QName(entry.getKey())), value);
        }

        // Activiti is confusing when it comes to names vs ids, ids are names with something like :1:4 at the end.
        // Clients are however used to starting workflows without that trailing part, so we support that too.
        if (workflowService.getDefinitionById(definitionId) == null) {
            definitionId = workflowService.getDefinitionByName(definitionId).getId();
        }

        WorkflowPath wfPath = workflowService.startWorkflow(definitionId, convertedParams);
        return (Workflow) this.alfredApiWfProcQueryConverter.get(wfPath.getId());
    }

    private static boolean isNodeRef(Serializable value) {
        if (!(value instanceof String)) {
            return false;
        }
        String v = (String) value;
        return v.matches(
                "(workspace|archive|user|system)://((Spaces|version2|lightWeightVersion|alfrescoUser)Store|system)/.*");
    }
}
