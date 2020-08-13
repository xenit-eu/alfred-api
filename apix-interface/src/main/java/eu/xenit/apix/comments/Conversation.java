package eu.xenit.apix.comments;

import java.util.List;
import java.util.Objects;

public class Conversation {

    private List<Comment> comments;
    private boolean hasMore;
    private boolean creatable;

    public Conversation() {
    }

    public Conversation(List<Comment> comments, boolean hasMore, boolean creatable) {
        this.comments = comments;
        this.hasMore = hasMore;
        this.creatable = creatable;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public boolean isCreatable() {
        return creatable;
    }

    public void setCreatable(boolean creatable) {
        this.creatable = creatable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conversation)) {
            return false;
        }
        Conversation that = (Conversation) o;
        return isHasMore() == that.isHasMore() &&
                isCreatable() == that.isCreatable() &&
                getComments().equals(that.getComments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getComments(), isHasMore(), isCreatable());
    }
}

