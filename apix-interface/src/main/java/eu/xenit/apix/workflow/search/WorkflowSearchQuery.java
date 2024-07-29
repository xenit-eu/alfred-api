package eu.xenit.apix.workflow.search;

public class WorkflowSearchQuery extends SearchQuery {

    public QueryScope scope;

    public WorkflowSearchQuery() {
    }

    public WorkflowSearchQuery(WorkflowSearchQuery source) {
        super(source);
        this.scope = source.scope;
    }

    @Override
    public void restrictResultsToUser(String currentUserName) {
        switch (scope) {
            case WorkflowsIveStarted:
                this.filters.add(new PropertyFilter(currentUserName, "{http://www.alfresco.org/model/bpm/1.0}initiator",
                        PropertyFilter.TYPE));
                return;
            case AllWorkflows:
            default:
                this.filters.add(new PropertyFilter(currentUserName, "{http://www.alfresco.org/model/bpm/1.0}involved",
                        PropertyFilter.TYPE));
        }
    }

    public enum QueryScope {
        WorkflowsIveStarted,
        AllWorkflows,
    }
}
