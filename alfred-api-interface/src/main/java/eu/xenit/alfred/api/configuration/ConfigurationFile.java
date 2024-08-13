package eu.xenit.alfred.api.configuration;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.filefolder.NodePath;
import eu.xenit.alfred.api.node.NodeMetadata;

public class ConfigurationFile {

    private String content;
    private NodeRef nodeRef;
    private NodeMetadata metadata;
    private NodePath path;
    private Object parsedContent;

    public ConfigurationFile(NodeRef nodeRef, NodeMetadata metadata) {
        this.nodeRef = nodeRef;
        this.metadata = metadata;
    }

    public ConfigurationFile() {
    }

    public ConfigurationFile(String content, NodeRef nodeRef, NodeMetadata metadata, NodePath path,
            Object parsedContent) {
        this.content = content;
        this.nodeRef = nodeRef;
        this.metadata = metadata;
        this.path = path;
        this.parsedContent = parsedContent;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NodeMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(NodeMetadata metadata) {
        this.metadata = metadata;
    }

    public NodePath getPath() {
        return path;
    }

    public void setPath(NodePath path) {
        this.path = path;
    }

    public Object getParsedContent() {
        return parsedContent;
    }

    public void setParsedContent(Object parsedContent) {
        this.parsedContent = parsedContent;
    }
}
