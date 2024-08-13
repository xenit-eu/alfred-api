package eu.xenit.alfred.api.alfresco.workflow.activiti.query;

import eu.xenit.alfred.api.workflow.IWorkflowService;
import eu.xenit.alfred.api.workflow.search.DateRangeFilter;
import eu.xenit.alfred.api.workflow.search.IQueryFilter;
import eu.xenit.alfred.api.workflow.search.PropertyFilter;
import eu.xenit.alfred.api.workflow.search.Sorting;
import java.util.List;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlfredApiHistoricProcessInstanceQuery extends AlfredApiHistoricInstanceQuery {

    private final Logger logger = LoggerFactory.getLogger(AlfredApiHistoricProcessInstanceQuery.class);
    private HistoricProcessInstanceQuery process;

    AlfredApiHistoricProcessInstanceQuery(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
        this.process = this.getHistoryService().createHistoricProcessInstanceQuery();
    }

    public void setActive(boolean isActive) {
        if (isActive) {
            this.process.unfinished();
        } else {
            this.process.finished();
        }

    }

    public void orderBy(List<Sorting> orderBy) {
        if (orderBy == null || orderBy.size() == 0) {
            return;
        }
        for (Sorting sorting : orderBy) {
            switch (sorting.property) {
                case IWorkflowService.ALFRESCO_STARTDATE:
                    setOrdering(this.process.orderByProcessInstanceStartTime(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_ENDDATE:
                    setOrdering(this.process.orderByProcessInstanceEndTime(), sorting.order);
                    break;
                default:
                    logger.warn("Unsupported property to sort: " + sorting.property);
                    break;
            }
        }
    }

    public void filter(List<IQueryFilter> filters) {
        if (filters == null || filters.size() == 0) {
            return;
        }
        for (IQueryFilter filter : filters) {
            String filterType = filter.getType();
            switch (filterType) {
                case PropertyFilter.TYPE:
                    PropertyFilter propertyFilter = (PropertyFilter) filter;
                    processPropertyFilter(propertyFilter);
                    break;
                case DateRangeFilter.TYPE:
                    DateRangeFilter dateRangeFilter = (DateRangeFilter) filter;
                    processDateRangeFilter(dateRangeFilter);
                    break;
                default:
                    this.logger.warn("Unknown filter type: " + filterType);
                    break;
            }
        }
    }

    private void processPropertyFilter(PropertyFilter filter) {
        String filterQName = filter.getProperty();
        String activitiQName = this.getShortQName(filterQName);
        String filterValue = filter.getValue();
        this.logger.debug("Got property: " + filterQName + " with value: " + filterValue);

        switch (filterQName) {
            case IWorkflowService.ALFRESCO_WF_PRIORITY:
            case IWorkflowService.ALFRESCO_PRIORITY:
                if (activitiQName == null) {
                    this.logger.warn("activitiQName is null for property QName provided: '" + filterQName + "'");
                    break;
                }
                this.process.variableValueEquals(activitiQName, filter.getIntValue());
                break;
            case IWorkflowService.ALFRESCO_ASSIGNEE:
            case IWorkflowService.ALFRESCO_GROUP_ASSIGNEE:
                if (activitiQName == null) {
                    this.logger.warn("activitiQName is null for property QName provided: '"
                            + IWorkflowService.ALFRESCO_ASSIGNEE + "'");
                    break;
                }
                this.setUserVariable(activitiQName, filterValue);
                break;
            case IWorkflowService.ALFRESCO_INITIATOR:
                this.process.startedBy(filterValue);
                break;
            case IWorkflowService.ALFRESCO_INVOLVED:
                this.process.involvedUser(filterValue);
                break;
            default:
                this.logger.debug("Ignoring PropertyFiled parameter: " + filterQName + " with value " + filterValue);
        }
    }

    private void processDateRangeFilter(DateRangeFilter dateRangeFilter) {
        String filterQName = dateRangeFilter.getProperty();
        this.logger.debug("DateRangeFilter: Got authority: " + filterQName
                + " with start date: " + dateRangeFilter.getStartDate()
                + " and end date: " + dateRangeFilter.getEndDate());

        switch (filterQName) {
            case IWorkflowService.ALFRESCO_ENDDATE:
                this.process.finishedAfter(dateRangeFilter.getStartDate());
                this.process.finishedBefore(dateRangeFilter.getEndDate());
                break;
            case IWorkflowService.ALFRESCO_STARTDATE:
                this.process.startedAfter(dateRangeFilter.getStartDate());
                this.process.startedBefore(dateRangeFilter.getEndDate());
                break;
            default:
                this.logger.warn("Unsupported DateRangeFilter type: " + filterQName);
                break;
        }
    }

    private void setUserVariable(String qname, String value) {
        ScriptNode assignee = this.getUserScriptNode(value);
        if (assignee != null) {
            this.process.variableValueEquals(qname, assignee);
        } else {
            this.logger.warn("The user was not found! User name provided: " + value);
        }

    }

    public <T> List<T> getAll() {
        return (List<T>) this.process.list();
    }
}