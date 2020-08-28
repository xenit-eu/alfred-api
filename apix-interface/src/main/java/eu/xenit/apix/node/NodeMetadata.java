package eu.xenit.apix.node;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;

import java.util.List;
import java.util.Map;

/**
 * Represents the metadata of a node. This contains The id, which is a noderef. The type of the node, which is a qname.
 * The basetype of the node, which is a qname. The transaction id. This long is used for caching purposes. The
 * properties, which is a Map from QName to String list. The aspects of the node, which is a list of qnames.
 */
public class NodeMetadata {
    @JsonSubTypes({
            @JsonSubTypes.Type(value=NodeRef.class, name = "id")
    })
    public NodeRef id;
    public QName type; // qname
    public QName baseType; //qname
    public long transactionId;
    public Map<QName, List<String>> properties;//: { [k: string]: Translation[]; }
    public List<QName> aspects;
    //TODO: parent

    //Removed in favor of using the permissions endpoint public boolean canEditMetadata; // TODO: remove

    //public NodeAssociation[] associations;

    @JsonCreator
    public NodeMetadata(
            @JsonProperty("id") NodeRef id,
            @JsonProperty("type") QName type,
            @JsonProperty("baseType") QName baseType,
            @JsonProperty("transactionId") long transactionId,
            @JsonProperty("properties") Map<QName, List<String>> properties,
            @JsonProperty("aspects") List<QName> aspects) {
        this.id = id;
        this.type = type;
        this.baseType = baseType;
        this.transactionId = transactionId;
        this.properties = properties;
        this.aspects = aspects;
    }

    public NodeRef getId() {
        return id;
    }

    public void setId(NodeRef id) {
        this.id = id;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public QName getBaseType() {
        return baseType;
    }

    public void setBaseType(QName baseType) {
        this.baseType = baseType;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public Map<QName, List<String>> getProperties() {
        return properties;
    }

    public void setProperties(Map<QName, List<String>> properties) {
        this.properties = properties;
    }

    public List<QName> getAspects() {
        return aspects;
    }

    public void setAspects(List<QName> aspects) {
        this.aspects = aspects;
    }


    @Override
    public String toString() {
        return "NodeMetadata{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", baseType=" + baseType + '\'' +
                ", transactionId=" + transactionId +
                ", properties=" + (properties != null ? properties.entrySet() : "") +
                ", aspects=" + aspects +
                '}';
    }
}
