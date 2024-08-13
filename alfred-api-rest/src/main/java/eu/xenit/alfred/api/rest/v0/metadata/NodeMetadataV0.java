package eu.xenit.alfred.api.rest.v0.metadata;


import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.node.NodeMetadata;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.permissions.PermissionValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class NodeMetadataV0 {

    public boolean canEditMetadata; // TODO: remove
    private String id;
    private String type; // qname
    //public String qnamePath;// Whats difference between qname and qnamepath
    //public String icon16; // REMOVE
    //public String icon32; // REMOVE
    //public String mimetype; // REMOVE
    //public boolean isVersioned; // REMOVE
    private long transactionId;
    //TODO: cleanup
    private Map<String, List<TranslationV0>> properties;//: { [k: string]: Translation[]; }
    //TODO: associations
    //TODO: parent
    private List<String> aspects;

    //public NodeAssociation[] associations;

    //public boolean hasChildren; // There is not getchildren?
    //public numberOfComments : int; do this using commentservice?

    public static NodeMetadataV0 FromV1(NodeMetadata m, IPermissionService permissionService) {
        NodeMetadataV0 ret = new NodeMetadataV0();
        ret.id = m.getId().getValue();
        ret.type = m.getType().getValue();
        ret.transactionId = m.getTransactionId();
        ret.properties = new HashMap<>();
        ret.aspects = new ArrayList<>();

        for (Map.Entry<QName, List<String>> e : m.getProperties().entrySet()) {
            List<TranslationV0> ts = null;
            if (e != null) {
                ts = new ArrayList<>();
                for (String t : e.getValue()) {
                    ts.add(new TranslationV0(t, t));
                }
            }
            ret.properties.put(e.getKey().getValue(), ts);
        }

        for (QName a : m.getAspects()) {
            ret.aspects.add(a.getValue());
        }

        Map<String, PermissionValue> perms = permissionService.getPermissions(m.getId());
        ret.canEditMetadata = perms.containsKey(IPermissionService.WRITE)
                && perms.get(IPermissionService.WRITE) == PermissionValue.ALLOW;

        return ret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public Map<String, List<TranslationV0>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, List<TranslationV0>> properties) {
        this.properties = properties;
    }

    public List<String> getAspects() {
        return aspects;
    }

    public void setAspects(List<String> aspects) {
        this.aspects = aspects;
    }

    public boolean isCanEditMetadata() {
        return canEditMetadata;
    }

    public void setCanEditMetadata(boolean canEditMetadata) {
        this.canEditMetadata = canEditMetadata;
    }

    @Override
    public String toString() {
        return "NodeMetadata{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", transactionId=" + transactionId +
                ", properties=" + (properties != null ? properties.entrySet() : "") +
                ", aspects=" + aspects +
                ", canEditMetadata=" + canEditMetadata +
                '}';
    }
}
