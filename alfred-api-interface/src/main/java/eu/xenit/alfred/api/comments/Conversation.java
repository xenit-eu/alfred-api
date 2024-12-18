package eu.xenit.alfred.api.comments;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Conversation {

    private List<Comment> comments;
    @JsonProperty("hasMore")
    private boolean hasMore;
    @JsonProperty("isCreatable")
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

    public boolean hasMore() {
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
        if (!(o instanceof Conversation that)) {
            return false;
        }
        return hasMore() == that.hasMore() &&
                isCreatable() == that.isCreatable() &&
                getComments().equals(that.getComments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getComments(), hasMore(), isCreatable());
    }
}

