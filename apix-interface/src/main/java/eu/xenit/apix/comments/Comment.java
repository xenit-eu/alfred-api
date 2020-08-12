package eu.xenit.apix.comments;

import eu.xenit.apix.data.NodeRef;
import java.util.Objects;

public class Comment {

    private NodeRef id;
    private String title;
    private String content;
    private String createdAt;
    private String createdBy;
    private String modifiedAt;
    private String modifiedBy;
    private boolean canEdit;
    private boolean canDelete;

    public NodeRef getId() {
        return id;
    }

    public void setId(NodeRef id) {
        this.id = id;
    }

    public Comment id(NodeRef id) {
        setId(id);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Comment title(String title) {
        setTitle(title);
        return this;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Comment content(String content) {
        setContent(content);
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Comment createdAt(String createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Comment createdBy(String createdBy) {
        setCreatedBy(createdBy);
        return this;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Comment modifiedAt(String modifiedAt) {
        setModifiedAt(modifiedAt);
        return this;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Comment modifiedBy(String modifiedBy) {
        setModifiedBy(modifiedBy);
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Comment canEdit(boolean canEdit) {
        setCanEdit(canEdit);
        return this;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public Comment canDelete(boolean canDelete) {
        setCanDelete(canDelete);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comment)) {
            return false;
        }
        Comment comment = (Comment) o;
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
