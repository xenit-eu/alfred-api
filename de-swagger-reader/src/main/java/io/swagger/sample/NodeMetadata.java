package io.swagger.sample;


import java.util.List;
import java.util.Map;

/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class NodeMetadata {

    public String id;
    public String type; // qname
    //public String qnamePath;// Whats difference between qname and qnamepath
    //public String icon16; // REMOVE
    //public String icon32; // REMOVE
    //public String mimetype; // REMOVE
    //public boolean isVersioned; // REMOVE
    public long transactionId;
    //TODO: cleanup
    public Map<String, List<String>> properties;//: { [k: string]: Translation[]; }
    public List<String> aspects;
    //TODO: associations
    //TODO: parent
    public String parent;
    public boolean canEditMetadata; // TODO: might not be a good place

    //public boolean hasChildren; // There is not getchildren?
    //public numberOfComments : int; do this using commentservice?


    @Override
    public String toString() {
        return "NodeMetadata{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                /*", qnamePath='" + qnamePath + '\'' +
                ", icon16='" + icon16 + '\'' +
                ", icon32='" + icon32 + '\'' +
                ", mimetype='" + mimetype + '\'' +
                ", isVersioned=" + isVersioned +*/
                ", transactionId=" + transactionId +
                ", properties=" + properties.entrySet() +
                ", aspects=" + aspects +
                ", canEditMetadata=" + canEditMetadata +
                '}';
    }
}
