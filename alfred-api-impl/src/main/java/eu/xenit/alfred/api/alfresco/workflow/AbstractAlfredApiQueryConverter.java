package eu.xenit.alfred.api.alfresco.workflow;

import eu.xenit.alfred.api.alfresco.workflow.activiti.query.AlfredApiHistoricInstanceQuery;
import eu.xenit.alfred.api.alfresco.workflow.utils.DebugHelper;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.workflow.model.ITaskOrWorkflow;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import eu.xenit.alfred.api.workflow.search.Facets;
import eu.xenit.alfred.api.workflow.search.Paging;
import eu.xenit.alfred.api.workflow.search.SearchQuery;
import eu.xenit.alfred.api.workflow.search.TaskOrWorkflowSearchResult;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAlfredApiQueryConverter {

    private final Logger logger = LoggerFactory.getLogger(AbstractAlfredApiQueryConverter.class);

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private IPeopleService peopleService;

    protected ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }

    protected IPeopleService getPeopleService() {
        return this.peopleService;
    }

    protected abstract <T> ITaskOrWorkflow convert(T var1);

    public abstract AlfredApiHistoricInstanceQuery convertQuery(SearchQuery var1);

    public abstract ITaskOrWorkflow get(String var1);

    public abstract void update(String var1, WorkflowOrTaskChanges var2);

    protected void ApplySearchQuery(SearchQuery searchQuery, AlfredApiHistoricInstanceQuery q) {
        q.setActive(searchQuery.isActive);
        q.filter(searchQuery.filters);
        q.orderBy(searchQuery.orderBy);
    }

    <T> TaskOrWorkflowSearchResult CreateSearchResult(List<T> processInstances, boolean includeResults, Paging paging) {
        int totalSize = processInstances.size();
        if (this.resultIsEmpty(totalSize, paging)) {
            return new TaskOrWorkflowSearchResult(new ArrayList(), new ArrayList(), new Facets(), 0);
        }

        List<T> modifiableTasksCollection = new ArrayList();
        if (paging != null) {
            modifiableTasksCollection = totalSize == 1
                    ? (new ArrayList(processInstances)).subList(0, 1)
                    : (new ArrayList(processInstances)).subList(paging.skip, totalSize);
        }

        List<ITaskOrWorkflow> ts = new ArrayList();
        List<String> taskIds = new ArrayList();
        Facets facets = new Facets();
        int parsedInstances = 0;
        long start = System.nanoTime();
        if (includeResults) {
            for (T processInstance : modifiableTasksCollection) {
                if (paging != null && parsedInstances >= paging.limit) {
                    break;
                }
                ITaskOrWorkflow result = this.convert(processInstance);
                if (result != null) {
                    ts.add(result);
                    ++parsedInstances;
                } else {
                    this.logger.debug("[" + processInstance.getClass().getName()
                            + "] We received workflow object from Activiti, but could not retrieve it from Alfresco?");
                    --totalSize;
                }
            }

            this.logger.debug("ITaskOrWorkflow paged and converted:" + ts.size());
        }

        logger.debug("CreateSearchResult: totalSize = " + totalSize
                + "; paging.skip = " + paging.skip
                + "; paging.limit = " + paging.limit
                + "; modifiableTasksCollection = " + modifiableTasksCollection.size()
                + "; parsedInstances = " + parsedInstances);

        DebugHelper.PrintCurrentTimeElapsed(this.logger, "CreateSearchResult in: ", start);
        return new TaskOrWorkflowSearchResult(ts, taskIds, facets, totalSize);
    }

    private boolean resultIsEmpty(int totalSize, Paging paging) {
        if (totalSize == 0) {
            return true;
        } else if (paging == null) {
            return false;
        } else {
            return totalSize == 1 ? paging.skip > 0 : paging.skip >= totalSize;
        }
    }

    public abstract void generate(int var1, String var2);

    public abstract void end(String var1, String var2);

    public abstract void claim(String var1);

    public abstract void claim(String var1, String var2);

    public abstract void release(String var1);
}
