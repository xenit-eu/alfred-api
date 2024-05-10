package eu.xenit.apix.rest.staging.workflow;

public class WorkflowReleaseBody {
    private String id;

    public WorkflowReleaseBody() {
    }

    public WorkflowReleaseBody(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "WorkflowReleaseBody{" +
                "id='" + id + '\'' +
                '}';
    }
}
