package eu.xenit.alfred.api.comments;

import eu.xenit.alfred.api.data.NodeRef;
import java.util.Objects;

public class Comment {

    private NodeRef id;
    private String title;
    private String content;
    private String createdAt;
    private String createdBy;
    private String modifiedAt;
    private String modifiedBy;
    private boolean editable;
    private boolean deletable;

    public NodeRef getId() {
        return id;
    }

    public void setId(NodeRef id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comment comment)) {
            return false;
        }
        return getId().equals(comment.getId()) &&
                getContent().equals(comment.getContent()) &&
                Objects.equals(getCreatedBy(), comment.getCreatedBy()) &&
                Objects.equals(getModifiedBy(), comment.getModifiedBy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getContent(), getCreatedBy(), getModifiedBy());
    }
}
