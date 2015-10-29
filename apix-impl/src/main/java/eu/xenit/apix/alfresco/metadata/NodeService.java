package eu.xenit.apix.alfresco.metadata;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.node.NodeAssociation;
import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.node.NodeMetadata;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mhgam on 23/11/2015.
 */
@OsgiService
@Component("eu.xenit.apix.alfresco.metadata.NodeService")
public class NodeService implements INodeService {

    private final static String NAMESPACE_BEGIN = "" + '{';
    private final static Logger logger = LoggerFactory.getLogger(NodeService.class);
    private ApixToAlfrescoConversion c;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private ServiceRegistry serviceRegistry;
    private org.alfresco.service.cmr.repository.NodeService nodeService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private CopyService copyService;
    private FileFolderService fileFolderService;
    private CheckOutCheckInService checkoutCheckinService;
    private LockService lockService;
    private AuthenticationService authenticationService;
    private SearchService searchService;
    private AclService aclService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private org.alfresco.repo.forum.CommentService commentService;
    private AlfrescoPropertyConvertor propertyConvertor;
    private TempFileProvider tempFileProvider;


    public NodeService() {
    }

    public NodeService(ServiceRegistry serviceRegistry, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        this.c = apixToAlfrescoConversion;
        InitializeServices(serviceRegistry, apixToAlfrescoConversion);
    }

    @Autowired
    public void setC(ApixToAlfrescoConversion c) {
        this.c = c;
        if (this.serviceRegistry != null) {
            InitializeServices(serviceRegistry, c);
        }
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        if (this.c != null) {
            InitializeServices(serviceRegistry, c);
        }
    }

    private void InitializeServices(ServiceRegistry serviceRegistry,
            ApixToAlfrescoConversion apixToAlfrescoConversion) {
        permissionService = serviceRegistry.getPermissionService();
        dictionaryService = serviceRegistry.getDictionaryService();
        copyService = serviceRegistry.getCopyService();
        nodeService = serviceRegistry.getNodeService();
        namespaceService = serviceRegistry.getNamespaceService();
        fileFolderService = serviceRegistry.getFileFolderService();
        checkoutCheckinService = serviceRegistry.getCheckOutCheckInService();
        lockService = serviceRegistry.getLockService();
        authenticationService = serviceRegistry.getAuthenticationService();
        searchService = serviceRegistry.getSearchService();
        contentService = serviceRegistry.getContentService();
        propertyConvertor = new AlfrescoPropertyConvertor(dictionaryService, apixToAlfrescoConversion);
        mimetypeService = serviceRegistry.getMimetypeService();
    }

    public List<NodeMetadata> getMetadata(List<eu.xenit.apix.data.NodeRef> noderefs) {
        List<NodeMetadata> nodeMetadatas = new ArrayList<>(noderefs.size());
        for (eu.xenit.apix.data.NodeRef node : noderefs) {
            nodeMetadatas.add(getMetadata(node));
        }

        return nodeMetadatas;

//        return noderefs.stream().map(x -> getMetadata(x)).collect(Collectors.toList());
    }

    public NodeMetadata getMetadata(eu.xenit.apix.data.NodeRef noderef) {
        org.alfresco.service.cmr.repository.NodeRef alfNodeRef = c.alfresco(noderef);

        if (!nodeService.exists(alfNodeRef)) {
            NodeService.logger.warn("Node not found, returning null for " + noderef.toString());
            return null;
        }

        NodeRef.Status nodeStatus = nodeService.getNodeStatus(alfNodeRef);
        Long transactionId = null != nodeStatus ? nodeStatus.getDbTxnId() : -1;
        //eu.xenit.common.model.NodeRef parent = alfrescoNodeRefConvertor.toModelNodeRef(nodeService.getPrimaryParent(alfNodeRef).getParentRef());
        //TODO: aclid is not public API, make API-XXX
        //Long aclId = aclService.getAclIdForNodeRef(alfNodeRef);
        //int amountOfComments = commentService.listComments(alfNodeRef,  new PagingRequest(9000)).getPage().size();

        Map<QName, Serializable> properties = this.nodeService.getProperties(alfNodeRef);

        eu.xenit.apix.data.QName type = c.apix(nodeService.getType(alfNodeRef));
        eu.xenit.apix.data.QName baseType = getBaseType(type);
        logger.debug("baseType: " + baseType);

        Map<eu.xenit.apix.data.QName, List<String>> metaProperties;
        try {
            metaProperties = propertyConvertor.toModelPropertyValueList(properties);
        } catch (Exception e) {
            logger.error("Failed to get properties for " + noderef.toString());
            throw e;
        }
        Set<QName> aspects = nodeService.getAspects(alfNodeRef);
        List<eu.xenit.apix.data.QName> metaAspects = new ArrayList<>(aspects.size());
        for (QName qName : aspects) {
            metaAspects.add(c.apix(qName));
        }

        return new NodeMetadata(noderef, type, baseType, transactionId, metaProperties, metaAspects);
    }

    public eu.xenit.apix.data.NodeRef getRootNode(eu.xenit.apix.data.StoreRef storeRef) {
        return c.apix(nodeService.getRootNode(c.alfresco(storeRef)));
    }


    private eu.xenit.apix.data.QName getBaseType(eu.xenit.apix.data.QName type) {
        logger.debug("getBaseType(type = " + type + ")");
        if (isNullOrTypeBase(type)) {
            return null;
        }

        QName ret = c.alfresco(type);
        logger.debug("ret: " + ret);
        while (!this.isGrandParentType(ret, ContentModel.TYPE_BASE)) {
            TypeDefinition retTypeDef = this.dictionaryService.getType(ret);
            QName parentTypeName = retTypeDef.getParentName();

            if (isNullOrTypeBase(parentTypeName)) {
                break;
            }

            ret = parentTypeName;
        }
        return c.apix(ret);
    }

    private boolean isNullOrTypeBase(eu.xenit.apix.data.QName type) {
        return type == null || type.equals(ContentModel.TYPE_BASE);
    }

    private boolean isNullOrTypeBase(QName alfType) {
        return alfType == null || alfType.equals(ContentModel.TYPE_BASE);
    }

    private boolean isGrandParentType(QName type, QName grandParentType) {
        logger.debug("isGrandParentType(type = " + type + ", grandParentType = " + grandParentType + ")");
        TypeDefinition typeDef = this.dictionaryService.getType(type);
        QName parentTypeName = typeDef.getParentName();
        logger.debug("parentType from type definition: " + parentTypeName);
        if (parentTypeName == null) {
            return false;
        }

        TypeDefinition parentTypeDef = this.dictionaryService.getType(parentTypeName);
        QName grandParentTypeName = parentTypeDef.getParentName();
        logger.debug("parentType from parent type definition: " + grandParentTypeName);
        if (grandParentTypeName == null) {
            return false;
        }

        return grandParentTypeName.equals(grandParentType);
    }

    //It is important in this method that the type is set first because setting the properties/aspects first can cause some unwanted side effects.
    //For example take this scenario: Your node has type "type1" and 1 property ("property1"). According to the definition of "type1" there is
    //1 default property "property1". According to the definition of "type2", which is the parent of "type1", there are no default properties.
    //Now I want to change the type of the node from "type1" to "type2" and I also want to set property "property1" to "something". Because
    //"type2" is the parent of "type1" we are generalizing the type of the node instead of specializing it. The order of the metadata changes
    //here is important. If we first set "property1" to "something" and afterwards change the type then "property1" will be removed and the type
    //will be set to "type2". If we first change the type then "property1" will be removed and the type will be set to "type2" but then we add
    // the property back and set it to "something". So we end up with different results.
    public NodeMetadata setMetadata(eu.xenit.apix.data.NodeRef noderef, MetadataChanges metadata) {
        NodeRef alfNode = c.alfresco(noderef);
        //TODO: remove conversion from the service
        if (metadata.getType() != null) {
            QName newTypeQName = c.alfresco(metadata.getType());
            QName oldTypeQName = nodeService.getType(alfNode);
            // check for type change
            if (!newTypeQName.equals(oldTypeQName)) {
                nodeService.setType(alfNode, newTypeQName);
                cleanupAspects(noderef, c.apix(oldTypeQName), c.apix(newTypeQName),
                        metadata.hasCleanUpAspectsOnGeneralization());
            }
        }

        Map<QName, Serializable> propertiesToSet = null;
        if (metadata.getPropertiesToSet() != null) {
            propertiesToSet = toAlfrescoPropertyMap(metadata.getPropertiesToSet());
        }

        if (metadata.getAspectsToAdd() != null) {
            Set<QName> aspectsToAdd = toAlfrescoQNameSet(metadata.getAspectsToAdd());

            for (QName alfQName : aspectsToAdd) {
                // check if all mandatory props are there to
                AspectDefinition aspectDefinition = dictionaryService.getAspect(alfQName);
                if (aspectDefinition == null) {
                    throw new RuntimeException();
                }

                Set<QName> manProps = new HashSet<>();
                for (Map.Entry<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> aspectProp : aspectDefinition
                        .getProperties().entrySet()) {
                    if (aspectProp.getValue().isMandatoryEnforced() || aspectProp.getValue().isMandatory()) {
                        manProps.add(aspectProp.getKey());
                    }
                }

                Map<QName, Serializable> manPropValues = new HashMap<>();
                for (QName manPropQName : manProps) {
                    if (propertiesToSet != null && propertiesToSet.containsKey(manPropQName)) {
                        manPropValues.put(manPropQName, propertiesToSet.get(manPropQName));
                        continue;
                    }

                    throw new RuntimeException(
                            "Mandatory property " + manPropQName.toString() + " for " + alfQName.toString()
                                    + " is missing. Unable to update metadata");
                }

                nodeService.addAspect(alfNode, alfQName, manPropValues);
            }
        }

        if (metadata.getAspectsToRemove() != null) {
            Set<QName> aspectsToRemove = toAlfrescoQNameSet(metadata.getAspectsToRemove());

            for (QName alfQName : aspectsToRemove) {
                nodeService.removeAspect(alfNode, alfQName);
            }
        }

        if (propertiesToSet != null) {
            for (Map.Entry<QName, Serializable> pair : propertiesToSet.entrySet()) {
                nodeService.setProperty(alfNode, pair.getKey(), pair.getValue());
            }
        }

        return this.getMetadata(noderef);
    }

    protected void cleanupAspects(eu.xenit.apix.data.NodeRef nodeRef, eu.xenit.apix.data.QName oldTypeQName,
            eu.xenit.apix.data.QName newTypeQName, boolean cleanUp) {
        if (!cleanUp || !dictionaryService
                .isSubClass(c.alfresco(oldTypeQName), c.alfresco(newTypeQName))) {
            return;
        }
        TypeDefinition oldType = dictionaryService.getType(c.alfresco(oldTypeQName));
        TypeDefinition newType = dictionaryService.getType(c.alfresco(newTypeQName));

        // Get the diff of default/mandatory aspects
        List<AspectDefinition> aspects = oldType.getDefaultAspects(true);
        List<AspectDefinition> remainingAspects = newType.getDefaultAspects(true);

        List<AspectDefinition> aspectsToRemove = new ArrayList<>();
        for (AspectDefinition aspectDef : aspects) {
            if (!containsAspect(remainingAspects, aspectDef)) {
                aspectsToRemove.add(aspectDef);
            }
        }

        for (AspectDefinition aspectDefinition : aspectsToRemove) {
            logger.debug("-- removing " + aspectDefinition.getName() + " (and related properties)");
            nodeService.removeAspect(c.alfresco(nodeRef), aspectDefinition.getName());
        }
    }

    private boolean containsAspect(List<AspectDefinition> aspects, AspectDefinition containsAspect) {
        for (AspectDefinition aspect : aspects) {
            if (aspect.getName().toString().equals(containsAspect.getName().toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<ChildParentAssociation> getChildAssociations(eu.xenit.apix.data.NodeRef ref) {
        List<ChildParentAssociation> apixChildAssocs = new ArrayList<>();
        List<ChildAssociationRef> alfChildAssocs = this.nodeService.getChildAssocs(c.alfresco(ref));
        for (ChildAssociationRef alfChildAssoc : alfChildAssocs) {
            NodeRef alfChildRef = alfChildAssoc.getChildRef();
            QName alfType = alfChildAssoc.getTypeQName();
            boolean isPrimary = alfChildAssoc.isPrimary();
            ChildParentAssociation apixChildAssoc = new ChildParentAssociation(ref, c.apix(alfChildRef),
                    c.apix(alfType), isPrimary);
            apixChildAssocs.add(apixChildAssoc);
        }

        return apixChildAssocs;
    }

    @Override
    public List<ChildParentAssociation> getParentAssociations(eu.xenit.apix.data.NodeRef ref) {
        List<ChildParentAssociation> apixParentAssocs = new ArrayList<>();
        List<ChildAssociationRef> alfParentAssocs = this.nodeService.getParentAssocs(c.alfresco(ref));
        for (ChildAssociationRef alfParentAssoc : alfParentAssocs) {
            NodeRef alfParentRef = alfParentAssoc.getParentRef();
            QName alfType = alfParentAssoc.getTypeQName();
            boolean isPrimary = alfParentAssoc.isPrimary();
            ChildParentAssociation apixParentAssoc = new ChildParentAssociation(ref, c.apix(alfParentRef),
                    c.apix(alfType), isPrimary);
            apixParentAssocs.add(apixParentAssoc);
        }

        return apixParentAssocs;
    }

    @Override
    public List<NodeAssociation> getTargetAssociations(eu.xenit.apix.data.NodeRef ref) {
        List<NodeAssociation> apixPeerAssocs = new ArrayList<>();
        List<AssociationRef> alfPeerAssocs = this.nodeService
                .getTargetAssocs(c.alfresco(ref), RegexQNamePattern.MATCH_ALL);
        for (AssociationRef alfPeerAssoc : alfPeerAssocs) {
            NodeRef alfPeerRef = alfPeerAssoc.getTargetRef();
            QName alfType = alfPeerAssoc.getTypeQName();
            NodeAssociation apixPeerAssoc = new NodeAssociation(ref, c.apix(alfPeerRef), c.apix(alfType));
            apixPeerAssocs.add(apixPeerAssoc);
        }

        return apixPeerAssocs;
    }

    @Override
    public NodeAssociations getAssociations(eu.xenit.apix.data.NodeRef ref) {
        return new NodeAssociations(getChildAssociations(ref), getParentAssociations(ref), getTargetAssociations(ref));
    }

    @Override
    public void createAssociation(eu.xenit.apix.data.NodeRef source, eu.xenit.apix.data.NodeRef target,
            eu.xenit.apix.data.QName assocType) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source has to be different from target!");
        }
        nodeService.createAssociation(c.alfresco(source), c.alfresco(target), c.alfresco(assocType));
    }

    @Override
    public void removeAssociation(eu.xenit.apix.data.NodeRef source, eu.xenit.apix.data.NodeRef target,
            eu.xenit.apix.data.QName assocType) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source has to be different from target!");
        }
        nodeService.removeAssociation(c.alfresco(source), c.alfresco(target), c.alfresco(assocType));
    }


    private QName toAlfrescoQName(String s) {
        return c.alfresco(new eu.xenit.apix.data.QName(s));
    }

    private Map<QName, Serializable> toAlfrescoPropertyMap(Map<eu.xenit.apix.data.QName, String[]> props) {
        //(Map.Entry<String, String[]>)

        Map<QName, Serializable> alfProps = new HashMap<>(props.size());

        for (Map.Entry<eu.xenit.apix.data.QName, String[]> entry : props.entrySet()) {
            Serializable value;

            if (entry.getValue() == null) {
                value = null;
            }

            if (entry.getValue().length == 0) {
                value = null;
            } else if (entry.getValue().length == 1) {
                value = entry.getValue()[0];
            } else {
                value = (Serializable) Arrays.asList(entry.getValue());
            }

            alfProps.put(c.alfresco(entry.getKey()), value);
        }

//        return props.entrySet().stream()
//                .collect(Collectors.<Map.Entry<String, String[]>, QName, Serializable>toMap(
//                        e -> toAlfrescoQName(e.getKey()),
//                        e -> {
//                            if (e.getValue() == null) return null;
//                            if (e.getValue().length == 0) return null;
//                            else if (e.getValue().length == 1) return e.getValue()[0];
//
//                            new UnsupportedOperationException("Multivalued properties not supported yet!");
//                            return null;
//                        }));

        return alfProps;
    }

    private Set<QName> toAlfrescoQNameSet(eu.xenit.apix.data.QName[] aspectsToAdd) {
        Set<QName> qnames = new HashSet<>(aspectsToAdd.length);

        for (eu.xenit.apix.data.QName aspect : aspectsToAdd) {
            //qnames.add(QName.createQName(aspect));
            qnames.add(c.alfresco(aspect));
        }

        return qnames;

//        return Arrays.stream(aspectsToAdd).map(s -> QName.createQName(s)).collect(Collectors.toSet());
    }

    @Override
    public eu.xenit.apix.data.NodeRef copyNode(eu.xenit.apix.data.NodeRef source,
            eu.xenit.apix.data.NodeRef destination, boolean deepCopy) {
        NodeRef copyRef = this.copyService.copyAndRename(c.alfresco(source),
                c.alfresco(destination),
                ContentModel.ASSOC_CONTAINS,
                (QName) null, deepCopy);
        return c.apix(copyRef);
    }

    @Override
    public eu.xenit.apix.data.NodeRef moveNode(eu.xenit.apix.data.NodeRef source,
            eu.xenit.apix.data.NodeRef destination) {
        ChildAssociationRef primaryParentAssoc = this.nodeService.getPrimaryParent(c.alfresco(source));
        ChildAssociationRef childAssoc = this.nodeService.moveNode(c.alfresco(source),
                c.alfresco(destination),
                ContentModel.ASSOC_CONTAINS,
                primaryParentAssoc.getQName());
        return c.apix(childAssoc.getChildRef());
    }

    @Override
    public eu.xenit.apix.data.NodeRef createNode(eu.xenit.apix.data.NodeRef parent, String name,
            eu.xenit.apix.data.QName type) {
        if (parent == null) {
            throw new InvalidArgumentException("Parent cannot be null!");
        }
        if (name == null) {
            throw new InvalidArgumentException("Name cannot be null!");
        }
        if (type == null) {
            throw new InvalidArgumentException("Type cannot be null!");
        }
        FileInfo createdNode = fileFolderService.create(c.alfresco(parent), name, c.alfresco(type));
        return c.apix(createdNode.getNodeRef());
    }

    @Override
    public eu.xenit.apix.data.NodeRef createNode(eu.xenit.apix.data.NodeRef parent,
            Map<eu.xenit.apix.data.QName, String[]> properties, eu.xenit.apix.data.QName type,
            eu.xenit.apix.data.ContentData contentData) {
        String[] names = properties.get(c.apix(ContentModel.PROP_NAME));
        if (names == null || names.length == 0) {
            throw new InvalidArgumentException(
                    String.format("mandatory property %s is missing in properties", ContentModel.PROP_NAME));
        }
        String name = names[0];

        ChildAssociationRef result = null;
        try {
            result = nodeService.createNode(
                    c.alfresco(parent),
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
                    c.alfresco(type),
                    toAlfrescoPropertyMap(properties));

            if (contentData != null) {
                this.nodeService.setProperty(result.getChildRef(), ContentModel.PROP_CONTENT, c.alfresco(contentData));
            }
        } catch (DuplicateChildNodeNameException e) {
            throw new InvalidArgumentException("Node with same name already exists in  parent!");
        }

        return c.apix(result.getChildRef());

    }

    @Override
    public void setContent(eu.xenit.apix.data.NodeRef node, InputStream inputStream, String originalFilename) {
        if (inputStream == null) {
            nodeService.removeProperty(c.alfresco(node), ContentModel.PROP_CONTENT);
            return;
        }
        InputStream inputStreamCopy = null;
        try {
            inputStreamCopy = cloneInputStreamWithMarkSupported(inputStream);
            String mimeType = guessMimetype(originalFilename, inputStreamCopy);

            org.alfresco.service.cmr.repository.NodeRef createdNodeRef = c.alfresco(node);
            ContentWriter writer = contentService.getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);
            writer.putContent(inputStreamCopy);
            ContentData contentData = (ContentData) nodeService.getProperty(createdNodeRef, ContentModel.PROP_CONTENT);
            contentData = ContentData.setMimetype(contentData, mimeType);
            nodeService.setProperty(createdNodeRef, ContentModel.PROP_CONTENT, contentData);
        } catch (IOException ioException) {
            logger.error("Error handling the io-streams:", ioException);
            throw new RuntimeException(ioException);
        } finally {
            if (inputStreamCopy != null) {
                IOUtils.closeQuietly(inputStreamCopy);
            }
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void setContent(eu.xenit.apix.data.NodeRef node, eu.xenit.apix.data.ContentData contentData) {
        ContentData alfContentData = new ContentData(
                contentData.getContentUrl(),
                contentData.getMimetype(),
                contentData.getSize(),
                contentData.getEncoding(),
                contentData.getLocale());
        this.nodeService.setProperty(c.alfresco(node), ContentModel.PROP_CONTENT, alfContentData);
    }


    @Override
    public eu.xenit.apix.data.ContentInputStream getContent(eu.xenit.apix.data.NodeRef nodeRef) {
        final ContentReader reader = contentService.getReader(c.alfresco(nodeRef), ContentModel.PROP_CONTENT);
        if (reader == null) {
            return null;
        }
        InputStream inputContentStream = new DelayedInputStream(reader);
        return new eu.xenit.apix.data.ContentInputStream(inputContentStream, reader.getMimetype(), reader.getSize(),
                reader.getEncoding(), reader.getLocale());
    }

    @Override
    public eu.xenit.apix.data.ContentData createContent(InputStream inputStream, String mimeType, String encoding) {
        try {
            ContentWriter writer = this.contentService.getWriter(null, ContentModel.PROP_CONTENT, false);
            writer.setMimetype(mimeType);
            writer.setEncoding(encoding);
            writer.putContent(inputStream);
            eu.xenit.apix.data.ContentData result = new eu.xenit.apix.data.ContentData(writer.getContentUrl(),
                    writer.getMimetype(), writer.getSize(), writer.getEncoding(), writer.getLocale());
            return result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public eu.xenit.apix.data.ContentData createContentWithMimetypeGuess(InputStream inputStream, String fileName,
            String encoding) {
        //Making copy of original inputstream because we want an inputstream that definitely supports mark/reset
        InputStream inputStreamCopy = null;
        try {
            inputStreamCopy = cloneInputStreamWithMarkSupported(inputStream);
            String mimeType = guessMimetype(fileName, inputStreamCopy);

            ContentWriter writer = contentService.getWriter(null, ContentModel.PROP_CONTENT, false);
            writer.setMimetype(mimeType);
            writer.setEncoding(encoding);
            writer.putContent(inputStreamCopy);
            return new eu.xenit.apix.data.ContentData(
                    writer.getContentUrl(),
                    writer.getMimetype(),
                    writer.getSize(),
                    writer.getEncoding(),
                    writer.getLocale());
        } catch (IOException ioException) {
            logger.error("Error handling the io-streams:", ioException);
            throw new RuntimeException(ioException);
        } finally {
            if (inputStreamCopy != null) {
                IOUtils.closeQuietly(inputStreamCopy);
            }
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Make copy of original inputstream supporting mark/reset, since calling Alfresco's services' methods moves the
     * stream read pointer and by using mark/reset we keep stream usable.
     */
    protected InputStream cloneInputStreamWithMarkSupported(InputStream stream) throws IOException {
        File tmpFile = null;
        try {
            // Write stream to file using Alfrescos temp file provider. Will clean up all temp files after set period (default 1h)
            tmpFile = TempFileProvider.createTempFile(stream, "Apix_NodeService_cloneInputstreamWithMarkSupported_", "_tmpFile");
            return new ByteArrayInputStream(FileUtils.readFileToByteArray(tmpFile));
        } catch (Exception exception) {
            logger.error("encountered an error while processing a temp file.", exception);
            throw new IOException(exception);
        }
    }

    protected String guessMimetype(String fileName, InputStream inputStream) throws IOException {
        inputStream.mark(Integer.MAX_VALUE);
        String mimetype = mimetypeService.guessMimetype(fileName, inputStream);
        inputStream.reset();
        return mimetype;
    }

    @Override
    public boolean deleteNode(eu.xenit.apix.data.NodeRef nodeRef) {
        return this.deleteNode(nodeRef, false);
    }

    @Override
    public boolean exists(eu.xenit.apix.data.NodeRef nodeRef) {
        return nodeService.exists(c.alfresco(nodeRef));
    }

    @Override
    public boolean deleteNode(eu.xenit.apix.data.NodeRef nodeRef, boolean permanently) {
        boolean success = false;
        NodeRef aNodeRef = c.alfresco(nodeRef);
        if (permanently) {
            this.nodeService.addAspect(aNodeRef, ContentModel.ASPECT_TEMPORARY, null);
        }
        if (this.nodeService.exists(aNodeRef)) {
            this.nodeService.deleteNode(aNodeRef);
            success = true;
        }

        return success;
    }

    @Override
    public eu.xenit.apix.data.NodeRef checkout(eu.xenit.apix.data.NodeRef original,
            eu.xenit.apix.data.NodeRef destination) {
        eu.xenit.apix.data.NodeRef workingCopy;

        LockStatus lockStatus = this.lockService.getLockStatus(c.alfresco(original));
        if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER) {
            logger.debug("Trying to checkout a working copy but this is not possible.");
            return null;
        }

        if (destination == null) {
            workingCopy = c.apix(this.checkoutCheckinService.checkout(c.alfresco(original)));
        } else {
            ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(c.alfresco(destination));
            workingCopy = c.apix(this.checkoutCheckinService.checkout(c.alfresco(original),
                    c.alfresco(destination),
                    ContentModel.ASSOC_CONTAINS,
                    childAssocRef.getQName()));
        }

        return workingCopy;
    }

    @Override
    public eu.xenit.apix.data.NodeRef checkin(eu.xenit.apix.data.NodeRef nodeRef, String comment,
            boolean majorVersion) {
        HashMap props = new HashMap(2, 1.0F);
        props.put("description", comment);
        props.put("versionType", majorVersion ? VersionType.MAJOR : VersionType.MINOR);

        eu.xenit.apix.data.NodeRef original = c.apix(this.checkoutCheckinService.checkin(c.alfresco(nodeRef), props));

        return original;
    }

    @Override
    public eu.xenit.apix.data.NodeRef cancelCheckout(eu.xenit.apix.data.NodeRef workingCopyRef) {
        NodeRef original = this.checkoutCheckinService.cancelCheckout(c.alfresco(workingCopyRef));
        return c.apix(original);
    }

    @Override
    public eu.xenit.apix.data.NodeRef getWorkingCopySource(eu.xenit.apix.data.NodeRef workingCopyRef) {
        NodeRef originalRef = this.checkoutCheckinService.getCheckedOut(c.alfresco(workingCopyRef));
        return c.apix(originalRef);
    }

    @Override
    public void extractMetadata(eu.xenit.apix.data.NodeRef node) {
        org.alfresco.service.cmr.repository.NodeRef alfrescoNodeRef = c.alfresco(node);
        logger.debug("Extracting metadata for {}", alfrescoNodeRef);
        serviceRegistry.getActionService()
                .executeAction(serviceRegistry.getActionService().createAction("extract-metadata"), alfrescoNodeRef);
    }
}
