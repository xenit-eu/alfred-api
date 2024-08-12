package eu.xenit.apix.workflow.search;

public class TaskSearchQuery extends SearchQuery {

    public TaskSearchQuery.QueryScope scope;

    public TaskSearchQuery() {
    }

    public TaskSearchQuery(TaskSearchQuery source) {
        super(source);
        this.scope = source.scope;
    }

    public void restrictResultsToUser(String currentUserName) {
        switch (this.scope) {
            case MyTasks:
                this.filters.add(new PropertyFilter(currentUserName, "{http://www.alfresco.org/model/bpm/1.0}assignee",
                        PropertyFilter.TYPE));
                return;
            case MyPooledTasks:
                this.filters.add(new AuthorityFilter(currentUserName, "candidate", AuthorityFilter.TYPE));
                return;
            case AllTasks:
            default:
                this.filters.add(new PropertyFilter(currentUserName, "{http://www.alfresco.org/model/bpm/1.0}involved",
                        PropertyFilter.TYPE));
        }
    }

    public enum QueryScope {
        MyTasks,
        MyPooledTasks,
        AllTasks,
    }
}
