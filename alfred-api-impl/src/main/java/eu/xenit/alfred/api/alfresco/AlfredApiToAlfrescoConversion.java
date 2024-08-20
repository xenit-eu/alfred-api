package eu.xenit.alfred.api.alfresco;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component("eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion")
public class AlfredApiToAlfrescoConversion {

    private static final Logger logger = LoggerFactory.getLogger(AlfredApiToAlfrescoConversion.class);
    private final String qnameRegex = "^(\\{.+\\}.+)$";
    private final Pattern qnamePattern = Pattern.compile(qnameRegex);
    private NamespaceService namespaceService;

    @Autowired
    public AlfredApiToAlfrescoConversion(ServiceRegistry serviceRegistry) {
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    public NodeRef alfresco(eu.xenit.alfred.api.data.NodeRef obj) {
        return new NodeRef(obj.getValue());
    }

    public StoreRef alfresco(eu.xenit.alfred.api.data.StoreRef obj) {
        return new StoreRef(obj.getValue());
    }

    public eu.xenit.alfred.api.data.NodeRef alfredApi(NodeRef obj) {
        if (obj == null) {
            return null;
        }
        return new eu.xenit.alfred.api.data.NodeRef(obj.toString());
    }

    public List<eu.xenit.alfred.api.data.NodeRef> alfredApi(List<NodeRef> obj) {
        //return obj.stream().map(n->alfredApi(n)).collect(Collectors.toList());
        List<eu.xenit.alfred.api.data.NodeRef> alfredApiNodes = new ArrayList<>(obj.size());
        for (NodeRef alfNode : obj) {
            alfredApiNodes.add(alfredApi(alfNode));
        }

        return alfredApiNodes;
    }

//    public org.alfresco.repo.forms.Item alfresco(Item alfredApiItem){
//        return new org.alfresco.repo.forms.Item(alfredApiItem.getKind(), alfredApiItem.getId());
//    }
//
//    public org.alfresco.repo.forms.FormData alfresco(FormData alfredApiFormData){
//        org.alfresco.repo.forms.FormData alfFormData = new org.alfresco.repo.forms.FormData();
//        Map<String, FieldData> alfredApiFieldDataMap = alfredApiFormData.getAllFieldData();
//        Set<String> alfredApiFieldNames = alfredApiFieldDataMap.keySet();
//
//        for(String alfredApiFieldName : alfredApiFieldNames){
//            FieldData alfredApiFieldData = alfredApiFieldDataMap.get(alfredApiFieldName);
//            alfFormData.addFieldData(alfredApiFieldData.getName(), alfredApiFieldData.getValue());
//        }
//
//        return alfFormData;
//    }

    public Set<eu.xenit.alfred.api.data.NodeRef> alfredApiNodeRefs(Set<NodeRef> obj) {
        Set<eu.xenit.alfred.api.data.NodeRef> alfredApiNodes = new HashSet<>(obj.size());
        for (NodeRef alfNode : obj) {
            alfredApiNodes.add(alfredApi(alfNode));
        }

        return alfredApiNodes;
    }

    public List<NodeRef> alfresco(List<eu.xenit.alfred.api.data.NodeRef> obj) {
        //return obj.stream().map(n->alfresco(n)).collect(Collectors.toList());

        List<NodeRef> alfNodes = new ArrayList<>(obj.size());
        for (eu.xenit.alfred.api.data.NodeRef alfredApiNode : obj) {
            alfNodes.add(alfresco(alfredApiNode));
        }

        return alfNodes;
    }

    public Set alfresco(Set s) {
        if (s.isEmpty()) {
            return s;
        }
        Object first = s.iterator().next();
        if (first instanceof eu.xenit.alfred.api.data.NodeRef) {
            return alfrescoNoderefs(s);
        } else if (first instanceof eu.xenit.alfred.api.data.QName) {
            return alfrescoQNames(s);
        } else if (first instanceof NodeRef) {
            return alfredApiNodeRefs(s);
        } else if (first instanceof QName) {
            return alfredApiQNames(s);
        } else {
            throw new UnsupportedOperationException(
                    "Unable to convertQuery Set<" + first.getClass().getName() + ">, class is not supported.");
        }

    }

    public Set<NodeRef> alfrescoNoderefs(Set<eu.xenit.alfred.api.data.NodeRef> obj) {
        Set<NodeRef> alfNodes = new HashSet<>(obj.size());
        for (eu.xenit.alfred.api.data.NodeRef alfredApiNode : obj) {
            alfNodes.add(alfresco(alfredApiNode));
        }

        return alfNodes;
    }

    public boolean HasAlfrescoQname(eu.xenit.alfred.api.data.QName qName) {
        try {
            org.alfresco.service.namespace.QName alfName = alfresco(qName);
            return alfName != null;
        } catch (Exception e) {
            logger.debug("Qname does not exist: " + qName);
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean HasAlfredApiQname(org.alfresco.service.namespace.QName qName) {
        try {
            eu.xenit.alfred.api.data.QName alfredApiName = alfredApi(qName);
            return alfredApiName != null;
        } catch (Exception e) {
            logger.debug("Qname does not exist: " + qName);
            logger.debug(e.toString());
            return false;
        }
    }

    public QName alfresco(eu.xenit.alfred.api.data.QName obj) {
        String qname = obj.toString();

        // check if qname is long or short type
        //if (qname.matches(this.qnameRegex)){
        if (qnamePattern.matcher(qname).matches()) {
            return org.alfresco.service.namespace.QName.createQName(qname);
        } else {
            return org.alfresco.service.namespace.QName.createQName(qname, namespaceService);
        }
    }

    public ContentData alfresco(eu.xenit.alfred.api.data.ContentData obj) {
        return new ContentData(obj.getContentUrl(), obj.getMimetype(), obj.getSize(), obj.getEncoding(),
                obj.getLocale());
    }


    public Path alfresco(eu.xenit.alfred.api.data.Path obj) {
        String pathValue = obj.toString();

        Path path = new Path();
        path.toString();
        return path;
    }

    public Set<QName> alfrescoQNames(Set<eu.xenit.alfred.api.data.QName> obj) {
        Set<QName> alfQnames = new HashSet<>(obj.size());
        for (eu.xenit.alfred.api.data.QName alfredApiQname : obj) {
            alfQnames.add(alfresco(alfredApiQname));
        }

        return alfQnames;
    }

    public Set<eu.xenit.alfred.api.data.QName> alfredApiQNames(Set<QName> obj) {
        Set<eu.xenit.alfred.api.data.QName> alfredApiQnames = new HashSet<>(obj.size());
        for (QName qname : obj) {
            alfredApiQnames.add(alfredApi(qname));
        }

        return alfredApiQnames;
    }

    public eu.xenit.alfred.api.data.QName alfredApi(QName obj) {
        return new eu.xenit.alfred.api.data.QName(obj.toString());
    }
}
