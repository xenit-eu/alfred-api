package eu.xenit.apix.tests.helperClasses.alfresco.entities;

import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class Node {

    private NodeRef nodeRef;
    private Map<QName, String[]> properties;
    private List<QName> aspects;
    private QName type;
    private long transactionId;

    public Node(NodeRef nodeRef, Map<QName, String[]> properties, List<QName> aspects, QName type, long transactionId) {
        this.nodeRef = nodeRef;
        this.properties = properties;
        this.aspects = aspects;
        this.type = type;
        this.transactionId = transactionId;
    }


    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public Map<QName, String[]> getProperties() {
        return properties;
    }

    public void setProperties(Map<QName, String[]> properties) {
        this.properties = properties;
    }

    public List<QName> getAspects() {
        return aspects;
    }

    public void setAspects(List<QName> aspects) {
        this.aspects = aspects;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }
}
