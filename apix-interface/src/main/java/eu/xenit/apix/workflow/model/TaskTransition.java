package eu.xenit.apix.workflow.model;

/**
 * Created by jasper on 29/09/17.
 */
public class TaskTransition {

    public String id;
    public String title;
    public String description;

    public TaskTransition(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public TaskTransition() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
