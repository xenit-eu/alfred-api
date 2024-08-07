package eu.xenit.apix.rest.staging.workflow;

public class WorkflowClaimsBody {

    private String id;
    private String userName;

    public WorkflowClaimsBody(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public WorkflowClaimsBody() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
