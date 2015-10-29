package eu.xenit.apix.rest.v0.metadata;


import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class NodeMetadataV0 {

    public String id;
    public String type; // qname
    //public String qnamePath;// Whats difference between qname and qnamepath
    //public String icon16; // REMOVE
    //public String icon32; // REMOVE
    //public String mimetype; // REMOVE
    //public boolean isVersioned; // REMOVE
    public long transactionId;
    //TODO: cleanup
    public Map<String, List<TranslationV0>> properties;//: { [k: string]: Translation[]; }
    public List<String> aspects;
    //TODO: associations
    //TODO: parent

    public boolean canEditMetadata; // TODO: remove

    //public NodeAssociation[] associations;

    //public boolean hasChildren; // There is not getchildren?
    //public numberOfComments : int; do this using commentservice?

    public static NodeMetadataV0 FromV1(NodeMetadata m, IPermissionService permissionService) {
        NodeMetadataV0 ret = new NodeMetadataV0();
        ret.id = m.id.getValue();
        ret.type = m.type.getValue();
        ret.transactionId = m.transactionId;
        ret.properties = new HashMap<>();
        ret.aspects = new ArrayList<>();

        for (Map.Entry<QName, List<String>> e : m.properties.entrySet()) {
            List<TranslationV0> ts = null;
            if (e != null) {
                ts = new ArrayList<>();
                for (String t : e.getValue()) {
                    ts.add(new TranslationV0(t, t));
                }
            }
            ret.properties.put(e.getKey().getValue(), ts);
        }

        for (QName a : m.aspects) {
            ret.aspects.add(a.getValue());
        }

        Map<String, PermissionValue> perms = permissionService.getPermissions(m.id);
        ret.canEditMetadata = perms.containsKey(IPermissionService.WRITE)
                && perms.get(IPermissionService.WRITE) == PermissionValue.ALLOW;

        return ret;
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
