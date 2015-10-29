package eu.xenit.apix.tests.helperClasses.alfresco.services;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.tests.helperClasses.alfresco.entities.Node;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import java.io.Serializable;
import java.util.*;

public class AlfrescoNodeServiceStub implements NodeService {

    private Map<eu.xenit.apix.data.NodeRef, Node> nodes;
    private ApixToAlfrescoConversion apixAlfrescoConverter;

    public AlfrescoNodeServiceStub(Map<eu.xenit.apix.data.NodeRef, Node> nodes,
            ApixToAlfrescoConversion apixAlfrescoConverter) {
        this.nodes = nodes;
        this.apixAlfrescoConverter = apixAlfrescoConverter;
    }

    @Override
    public List<StoreRef> getStores() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoreRef createStore(String protocol, String identifier) throws StoreExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteStore(StoreRef storeRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(StoreRef storeRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(NodeRef nodeRef) {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        return node != null;
    }

    @Override
    public NodeRef.Status getNodeStatus(NodeRef nodeRef) {
        long transactionId = 1;
        long dbid = 1;
        return new NodeRef.Status(transactionId, nodeRef, null, dbid, false);
    }

    @Override
    public NodeRef getNodeRef(Long nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName,
            QName nodeTypeQName) throws InvalidNodeRefException, InvalidTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName,
            QName nodeTypeQName, Map<QName, Serializable> properties)
            throws InvalidNodeRefException, InvalidTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName,
            QName assocQName) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChildAssociationIndex(ChildAssociationRef childAssocRef, int index)
            throws InvalidChildAssociationRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        if (node == null) {
            throw new RuntimeException("node with reference " + nodeRef.toString() + " not found");
        }

        return apixAlfrescoConverter.alfresco(node.getType());
    }

    @Override
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        node.setType(apixAlfrescoConverter.apix(typeQName));
    }

    @Override
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties)
            throws InvalidNodeRefException, InvalidAspectException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        List<eu.xenit.apix.data.QName> aspects = node.getAspects();
        aspects.add(apixAlfrescoConverter.apix(aspectTypeQName));
    }

    @Override
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        List<eu.xenit.apix.data.QName> aspects = node.getAspects();
        aspects.remove(apixAlfrescoConverter.apix(aspectTypeQName));
    }

    @Override
    public boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        List<eu.xenit.apix.data.QName> aspects = node.getAspects();
        return toAlfrescoAspectsSet(aspects);
    }

    private Set<QName> toAlfrescoAspectsSet(List<eu.xenit.apix.data.QName> apixAspects) {
        HashSet<QName> alfrescoAspectsSet = new HashSet<>();
        for (eu.xenit.apix.data.QName apixAspect : apixAspects) {
            QName alfrescoAspect = apixAlfrescoConverter.alfresco(apixAspect);
            alfrescoAspectsSet.add(alfrescoAspect);
        }

        return alfrescoAspectsSet;
    }

    @Override
    public void deleteNode(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName qname)
            throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> addChild(Collection<NodeRef> parentRefs, NodeRef childRef, QName assocTypeQName,
            QName qname) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        Map<eu.xenit.apix.data.QName, String[]> apixProperties = node.getProperties();
        return toAlfrescoPropertyMap(apixProperties);
    }

    private Map<QName, Serializable> toAlfrescoPropertyMap(Map<eu.xenit.apix.data.QName, String[]> props) {

        Map<QName, Serializable> alfProps = new HashMap<>(props.size());

        for (Map.Entry<eu.xenit.apix.data.QName, String[]> entry : props.entrySet()) {
            Serializable value;

            if (entry.getValue() == null) {
                alfProps.put(apixAlfrescoConverter.alfresco(entry.getKey()), null);
                continue;
            }

            if (entry.getValue().length == 0) {
                value = null;
            } else if (entry.getValue().length == 1) {
                value = entry.getValue()[0];
            } else {
                value = (Serializable) Arrays.asList(entry.getValue());
            }

            alfProps.put(apixAlfrescoConverter.alfresco(entry.getKey()), value);
        }

        return alfProps;
    }

    @Override
    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException {
        eu.xenit.apix.data.NodeRef apixNodeRef = apixAlfrescoConverter.apix(nodeRef);
        Node node = nodes.get(apixNodeRef);
        Map<eu.xenit.apix.data.QName, String[]> properties = node.getProperties();
        properties.put(apixAlfrescoConverter.apix(qname), (String[]) value);
    }

    @Override
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern, int maxResults, boolean preload) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern, boolean preload) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(NodeRef nodeRef, QName propertyQName,
            Serializable value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChildAssociationRef> getChildrenByName(NodeRef nodeRef, QName assocTypeQName,
            Collection<String> childNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(NodeRef parent,
            QName assocTypeQName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssociationRef getAssoc(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeRef getStoreArchiveNode(StoreRef storeRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName,
            QName assocQName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NodeRef> findNodes(FindNodeParameters params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int countChildAssocs(NodeRef nodeRef, boolean isPrimary) throws InvalidNodeRefException {
        throw new UnsupportedOperationException();
    }
}
