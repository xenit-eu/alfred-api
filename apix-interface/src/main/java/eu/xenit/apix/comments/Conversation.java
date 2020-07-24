package eu.xenit.apix.comments;

import java.util.List;

public class Conversation {

    private List<Comment> comments;
    private boolean hasMore;
    private boolean canCreate;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Conversation comments(List<Comment> comments) {
        setComments(comments);
        return this;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Conversation hasMore(boolean hasMore) {
        setHasMore(hasMore);
        return this;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Conversation canCreate(boolean canCreate) {
        setCanCreate(canCreate);
        return this;
    }
}

