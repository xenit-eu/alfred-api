package eu.xenit.apix.node;


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

    public NodeRef id;
    public QName type; // qname
    public QName baseType; //qname
    public long transactionId;
    public Map<QName, List<String>> properties;//: { [k: string]: Translation[]; }
    public List<QName> aspects;
    //TODO: parent

    //Removed in favor of using the permissions endpoint public boolean canEditMetadata; // TODO: remove

    //public NodeAssociation[] associations;

    public NodeMetadata(NodeRef id, QName type, QName baseType, long transactionId, Map<QName, List<String>> properties,
                        List<QName> aspects) {
        this.id = id;
        this.type = type;
        this.baseType = baseType;
        this.transactionId = transactionId;
        this.properties = properties;
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