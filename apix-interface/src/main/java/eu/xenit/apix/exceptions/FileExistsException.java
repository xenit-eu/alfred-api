package eu.xenit.apix.exceptions;

import eu.xenit.apix.data.NodeRef;

public class FileExistsException extends RuntimeException {

    private NodeRef source;
    private NodeRef parent;
    private String name;

    public FileExistsException() {
    }

    public FileExistsException(String message) {
        super(message);
    }

    public FileExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileExistsException(Throwable cause) {
        super(cause);
    }

    public FileExistsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FileExistsException(NodeRef source, NodeRef parent, String name) {
        this.source = source;
        this.parent = parent;
        this.name = name;
    }

    public FileExistsException(String message, NodeRef source, NodeRef parent, String name) {
        super(message);
        this.source = source;
        this.parent = parent;
        this.name = name;
    }

    public FileExistsException(String message, Throwable cause, NodeRef source, NodeRef parent, String name) {
        super(message, cause);
        this.source = source;
        this.parent = parent;
        this.name = name;
    }

    public FileExistsException(Throwable cause, NodeRef source, NodeRef parent, String name) {
        super(cause);
        this.source = source;
        this.parent = parent;
        this.name = name;
    }

    public FileExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
            NodeRef source, NodeRef parent, String name) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.source = source;
        this.parent = parent;
        this.name = name;
    }

    public NodeRef getSource() {
        return source;
    }

    public void setSource(NodeRef source) {
        this.source = source;
    }

    public NodeRef getParent() {
        return parent;
    }

    public void setParent(NodeRef parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuilder messagebuilder = new StringBuilder("A file with name '")
                .append(getName())
                .append("' already exists at ")
                .append(getParent());
        if (getSource() != null) {
            messagebuilder.append("\n");
            messagebuilder.append("The source node is ").append(getSource());
        }
        messagebuilder.append("\n");
        messagebuilder.append(super.toString());
        return messagebuilder.toString();
    }
}
