package eu.xenit.apix.alfresco.workflow.activiti.query;

import eu.xenit.apix.alfresco.workflow.activiti.ActivitiWorkflowTaskWorkflowConvertor;
import eu.xenit.apix.alfresco.workflow.utils.QueryStringFormatter;
import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.workflow.IWorkflowService;
import eu.xenit.apix.workflow.search.AuthorityFilter;
import eu.xenit.apix.workflow.search.DateRangeFilter;
import eu.xenit.apix.workflow.search.IQueryFilter;
import eu.xenit.apix.workflow.search.PropertyFilter;
import eu.xenit.apix.workflow.search.Sorting;

import java.util.Collections;
import java.util.List;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApixHistoricTaskInstanceQuery extends ApixHistoricInstanceQuery {

    private final Logger logger = LoggerFactory.getLogger(ApixHistoricTaskInstanceQuery.class);
    private TaskQuery task;

    private IPeopleService peopleService;

    ApixHistoricTaskInstanceQuery(ServiceRegistry serviceRegistry, IPeopleService peopleService) {
        super(serviceRegistry);
        this.peopleService = peopleService;
        this.task = this.getTaskService().createTaskQuery();
    }

    public void setActive(boolean isActive) {
        if (isActive) {
            this.task.active();
        } else {
            this.task.suspended();
        }

    }

    public void orderBy(List<Sorting> orderBy) {
        if (orderBy == null || orderBy.size() == 0) {
            return;
        }
        for (Sorting sorting : orderBy) {
            switch (sorting.property) {
                case IWorkflowService.ALFRESCO_BPM_NAME:
                case IWorkflowService.ALFRESCO_CM_NAME:
                    setOrdering(this.task.orderByTaskName(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_DESCRIPTION:
                    setOrdering(this.task.orderByTaskDescription(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_PRIORITY:
                    setOrdering(this.task.orderByTaskPriority(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_ASSIGNEE:
                    setOrdering(this.task.orderByTaskAssignee(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_DUEDATE:
                    setOrdering(this.task.orderByDueDate(), sorting.order);
                    break;
                case IWorkflowService.ALFRESCO_STARTDATE:
                    setOrdering(this.task.orderByTaskCreateTime(), sorting.order);
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
                case AuthorityFilter.TYPE:
                    AuthorityFilter authorityFilter = (AuthorityFilter) filter;
                    processAuthorityFilter(authorityFilter);
                    break;
                case DateRangeFilter.TYPE:
                    DateRangeFilter dateRangeFilter = (DateRangeFilter) filter;
                    processDateRangeFilter(dateRangeFilter);
                    break;
                default:
                    this.logger.warn("Unknown filter type: " + filter.getType());
                    break;
            }
        }
    }

    private void processPropertyFilter(PropertyFilter filter) {
        String filterQName = filter.getProperty();
        String filterValue = filter.getValue();

        this.logger.debug("PropertyFilter: Got PropertyFilter: " + filterQName + " with value: " + filterValue);
        String valueToCompare;
        switch (filterQName) {
            case IWorkflowService.ALFRESCO_BPM_NAME:
            case IWorkflowService.ALFRESCO_CM_NAME:
                valueToCompare = filterValue;
                if (QueryStringFormatter.isExactMatch(valueToCompare)) {
                    this.task.taskName(valueToCompare);
                    break;
                }
                valueToCompare = QueryStringFormatter.apply(valueToCompare);
                this.task.taskNameLike(valueToCompare);
                break;
            case IWorkflowService.ALFRESCO_DESCRIPTION:
                valueToCompare = filterValue;
                if (QueryStringFormatter.isExactMatch(valueToCompare)) {
                    this.task.taskDescription(valueToCompare);
                    break;
                }
                valueToCompare = QueryStringFormatter.apply(valueToCompare);
                this.task.taskDescriptionLike(valueToCompare);
                break;
            case IWorkflowService.ALFRESCO_PRIORITY:
                this.task.taskPriority(filter.getIntValue());
                break;
            case IWorkflowService.ALFRESCO_OWNER:
                this.task.taskOwner(filterValue);
                break;
            case IWorkflowService.ALFRESCO_ASSIGNEE:
                this.task.taskAssignee(filterValue);
                break;
            case IWorkflowService.ALFRESCO_INVOLVED:
                this.task.taskInvolvedUser(filterValue);
                break;
            case IWorkflowService.ALFRESCO_WORKFLOW_INSTANCE_ID:
                String wfId = filterValue.replace(ActivitiWorkflowTaskWorkflowConvertor.ACTIVITI_PREFIX, "");
                this.task.processInstanceId(wfId);
                break;
            default:
                this.logger.debug("Ignoring PropertyFiled parameter: " + filterQName + " with value " + filterValue);
        }
    }

    private void processAuthorityFilter(AuthorityFilter filter) {
        String filterQName = filter.getProperty();
        String filterValue = filter.getValue();

        this.logger.debug("AuthorityFilter: Got authority: " + filterQName + " with value: " + filterValue);
        switch (filterQName) {
            case IWorkflowService.ALFRESCO_OWNER:
                this.task.taskOwner(filterValue);
                break;
            case IWorkflowService.ALFRESCO_ASSIGNEE:
                this.task.taskAssignee(filterValue);
                break;
            case IWorkflowService.ALFRESCO_INVOLVED:
                this.task.taskInvolvedUser(filterValue);
                break;
            case "candidate":
                if (this.peopleService.isUser(filterValue)) {
                    this.task.taskCandidateUser(filterValue);
                    return;
                }
                if (this.peopleService.isGroup(filterValue)) {
                    this.task.taskCandidateGroupIn(Collections.singletonList(filterValue));
                    return;
                }
                // Otherwise fall through
            default:
                this.logger.warn("Unsupported authority type: " + filterValue);
                break;
        }
    }

    private void processDateRangeFilter(DateRangeFilter filter) {
        String filterQName = filter.getProperty();
        this.logger.debug("DateRangeFilter: Got DateRangeFilter: " + filterQName
                + " with start date: " + filter.getStartDate()
                + " and end date: " + filter.getEndDate());

        switch (filterQName) {
            case IWorkflowService.ALFRESCO_STARTDATE:
                this.task.taskCreatedAfter(filter.getStartDate());
                this.task.taskCreatedBefore(filter.getEndDate());
                break;
            case IWorkflowService.ALFRESCO_DUEDATE:
                this.task.dueAfter(filter.getStartDate());
                this.task.dueBefore(filter.getEndDate());
                break;
            default:
                this.logger.warn("Unsupported DateRangeFilter type: " + filterQName);
                break;
        }
    }

    public <T> List<T> getAll() {
        return (List<T>) this.task.list();
    }
}
