package eu.xenit.alfred.api.alfresco.metadata;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.ContentInputStream;
import eu.xenit.alfred.api.data.StoreRef;
import eu.xenit.alfred.api.node.ChildParentAssociation;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.MetadataChanges;
import eu.xenit.alfred.api.node.NodeAssociation;
import eu.xenit.alfred.api.node.NodeAssociations;
import eu.xenit.alfred.api.node.NodeMetadata;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.MimetypeServiceAware;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("eu.xenit.alfred.api.alfresco.metadata.NodeService")
public class NodeService implements INodeService {

    private final static String NAMESPACE_BEGIN = "" + '{';
    private final static Logger logger = LoggerFactory.getLogger(NodeService.class);
    private AlfredApiToAlfrescoConversion c;
    private NamespaceService namespaceService;
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
    private Repository repository;

    @Autowired
    public NodeService(ServiceRegistry serviceRegistry, AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion,
            Repository repository) {
        c = alfredApiToAlfrescoConversion;
        this.serviceRegistry = serviceRegistry;
        this.repository = repository;
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
        propertyConvertor = new AlfrescoPropertyConvertor(serviceRegistry, alfredApiToAlfrescoConversion);
        mimetypeService = serviceRegistry.getMimetypeService();
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public List<NodeMetadata> getMetadata(List<eu.xenit.alfred.api.data.NodeRef> noderefs) {
        List<NodeMetadata> nodeMetadatas = new ArrayList<>(noderefs.size());
        for (eu.xenit.alfred.api.data.NodeRef node : noderefs) {
            nodeMetadatas.add(getMetadata(node));
        }
        return nodeMetadatas;
    }

    public NodeMetadata getMetadata(eu.xenit.alfred.api.data.NodeRef noderef) {
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

        eu.xenit.alfred.api.data.QName type = c.alfredApi(nodeService.getType(alfNodeRef));
        eu.xenit.alfred.api.data.QName baseType = getBaseType(type);
        logger.debug("baseType: " + baseType);

        Map<eu.xenit.alfred.api.data.QName, List<String>> metaProperties;
        try {
            metaProperties = propertyConvertor.toModelPropertyValueList(properties);
        } catch (Exception e) {
            logger.error("Failed to get properties for " + noderef.toString());
            throw e;
        }
        Set<QName> aspects = nodeService.getAspects(alfNodeRef);
        List<eu.xenit.alfred.api.data.QName> metaAspects = new ArrayList<>(aspects.size());
        for (QName qName : aspects) {
            metaAspects.add(c.alfredApi(qName));
        }

        return new NodeMetadata(noderef, type, baseType, transactionId, metaProperties, metaAspects);
    }

    public eu.xenit.alfred.api.data.NodeRef getRootNode(StoreRef storeRef) {
        return c.alfredApi(nodeService.getRootNode(c.alfresco(storeRef)));
    }


    private eu.xenit.alfred.api.data.QName getBaseType(eu.xenit.alfred.api.data.QName type) {
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
        return c.alfredApi(ret);
    }

    private boolean isNullOrTypeBase(eu.xenit.alfred.api.data.QName type) {
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
    public NodeMetadata setMetadata(eu.xenit.alfred.api.data.NodeRef noderef, MetadataChanges metadata) {
        NodeRef alfNode = c.alfresco(noderef);
        //TODO: remove conversion from the service
        if (metadata.getType() != null) {
            QName newTypeQName = c.alfresco(metadata.getType());
            QName oldTypeQName = nodeService.getType(alfNode);
            // check for type change
            if (!newTypeQName.equals(oldTypeQName)) {
                nodeService.setType(alfNode, newTypeQName);
                if (metadata.hasCleanUpAspectsOnGeneralization() && dictionaryService
                        .isSubClass(oldTypeQName, newTypeQName)) {
                    cleanupAspects(noderef, c.alfredApi(oldTypeQName), c.alfredApi(newTypeQName));
                }
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
            Serializable namePropValue = propertiesToSet.remove(ContentModel.PROP_NAME);
            if (namePropValue != null) {
                renameNode(alfNode, DefaultTypeConverter.INSTANCE.convert(String.class, namePropValue));
            }
            nodeService.addProperties(alfNode, propertiesToSet);
        }

        return this.getMetadata(noderef);
    }

    private void renameNode(NodeRef nodeRef, String newName) {
        QName nodeType = nodeService.getType(nodeRef);
        if ((dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER) &&
                !dictionaryService.isSubClass(nodeType, ContentModel.TYPE_SYSTEM_FOLDER)) ||
                dictionaryService.isSubClass(nodeType, ContentModel.TYPE_CONTENT)) {
            try {
                fileFolderService.rename(nodeRef, newName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName);
        }
    }

    public void cleanupAspects(eu.xenit.alfred.api.data.NodeRef nodeRef, eu.xenit.alfred.api.data.QName oldTypeQName,
            eu.xenit.alfred.api.data.QName newTypeQName) {
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
    public List<ChildParentAssociation> getChildAssociations(eu.xenit.alfred.api.data.NodeRef ref) {
        return nodeService.getChildAssocs(c.alfresco(ref))
                .stream().map(alfChildAssoc ->
                        new ChildParentAssociation(
                                ref,
                                c.alfredApi(alfChildAssoc.getChildRef()),
                                c.alfredApi(alfChildAssoc.getTypeQName()),
                                alfChildAssoc.isPrimary()
                        ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChildParentAssociation> getParentAssociations(eu.xenit.alfred.api.data.NodeRef ref) {
        return nodeService.getParentAssocs(c.alfresco(ref))
                .stream().map(alfParentAssoc ->
                        new ChildParentAssociation(
                                ref,
                                c.alfredApi(alfParentAssoc.getParentRef()),
                                c.alfredApi(alfParentAssoc.getTypeQName()),
                                alfParentAssoc.isPrimary()
                        ))
                .collect(Collectors.toList());
    }

    @Override
    public List<NodeAssociation> getTargetAssociations(eu.xenit.alfred.api.data.NodeRef ref) {
        return nodeService.getTargetAssocs(c.alfresco(ref), RegexQNamePattern.MATCH_ALL)
                .stream()
                .map(alfPeerAssoc ->
                        new NodeAssociation(
                                ref,
                                c.alfredApi(alfPeerAssoc.getTargetRef()),
                                c.alfredApi(alfPeerAssoc.getTypeQName())))
                .collect(Collectors.toList());
    }

    @Override
    public List<NodeAssociation> getSourceAssociations(eu.xenit.alfred.api.data.NodeRef ref) {
        // Versionstore does not support sourceAssocs. For version nodes, do not do call, add empty list to result
        if ("versionStore".equals(ref.getStoreRefProtocol())) {
            return new ArrayList<>();
        }
        return nodeService.getSourceAssocs(c.alfresco(ref), RegexQNamePattern.MATCH_ALL)
                .stream()
                .map(alfPeerAssoc ->
                        new NodeAssociation(
                                c.alfredApi(alfPeerAssoc.getSourceRef()),
                                ref,
                                c.alfredApi(alfPeerAssoc.getTypeQName())))
                .collect(Collectors.toList());
    }

    @Override
    public NodeAssociations getAssociations(eu.xenit.alfred.api.data.NodeRef ref) {
        return new NodeAssociations(getChildAssociations(ref), getParentAssociations(ref), getTargetAssociations(ref),
                getSourceAssociations(ref));
    }

    @Override
    public List<eu.xenit.alfred.api.data.NodeRef> getAncestors(eu.xenit.alfred.api.data.NodeRef ref,
            eu.xenit.alfred.api.data.NodeRef rootRef) throws InvalidNodeRefException, AccessDeniedException {
        NodeRef alfrescoRootRef;
        if (rootRef == null) {
            alfrescoRootRef = repository.getCompanyHome();
        } else {
            alfrescoRootRef = c.alfresco(rootRef);
        }

        NodeRef nodeRef = c.alfresco(ref);
        if (nodeRef.equals(alfrescoRootRef)) {
            return new ArrayList<>();
        }

        String accessDeniedMessage = "no read access on node with node reference %s";
        String nodeDoesNotExistMesage = "node with node reference %s does not exist";
        List<eu.xenit.alfred.api.data.NodeRef> ancestorRefs = new ArrayList<>();
        if (!nodeService.exists(nodeRef)) {
            throw new InvalidNodeRefException(String.format(nodeDoesNotExistMesage, nodeRef), nodeRef);
        }
        if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
            throw new AccessDeniedException(String.format(accessDeniedMessage, nodeRef));
        }

        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);
        NodeRef parentRef = childAssocRef.getParentRef();
        while (parentRef != null) {
            ancestorRefs.add(c.alfredApi(parentRef));
            if (parentRef.equals(alfrescoRootRef)) {
                break;
            }
            if (permissionService.hasPermission(parentRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                throw new AccessDeniedException(String.format(accessDeniedMessage, parentRef));
            }
            ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(parentRef);
            parentRef = parentAssoc.getParentRef();
        }

        return ancestorRefs;
    }

    @Override
    public void createAssociation(eu.xenit.alfred.api.data.NodeRef source, eu.xenit.alfred.api.data.NodeRef target,
            eu.xenit.alfred.api.data.QName assocType) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source has to be different from target!");
        }
        nodeService.createAssociation(c.alfresco(source), c.alfresco(target), c.alfresco(assocType));
    }

    @Override
    public void removeAssociation(eu.xenit.alfred.api.data.NodeRef source, eu.xenit.alfred.api.data.NodeRef target,
            eu.xenit.alfred.api.data.QName assocType) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("Source has to be different from target!");
        }
        nodeService.removeAssociation(c.alfresco(source), c.alfresco(target), c.alfresco(assocType));
    }


    private QName toAlfrescoQName(String s) {
        return c.alfresco(new eu.xenit.alfred.api.data.QName(s));
    }

    private Map<QName, Serializable> toAlfrescoPropertyMap(Map<eu.xenit.alfred.api.data.QName, String[]> props) {
        Map<QName, Serializable> alfProps = new HashMap<>(props.size());
        for (Map.Entry<eu.xenit.alfred.api.data.QName, String[]> entry : props.entrySet()) {
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
        return alfProps;
    }

    private Set<QName> toAlfrescoQNameSet(eu.xenit.alfred.api.data.QName[] aspectsToAdd) {
        Set<QName> qnames = new HashSet<>(aspectsToAdd.length);
        for (eu.xenit.alfred.api.data.QName aspect : aspectsToAdd) {
            qnames.add(c.alfresco(aspect));
        }
        return qnames;
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef copyNode(eu.xenit.alfred.api.data.NodeRef source,
            eu.xenit.alfred.api.data.NodeRef destination, boolean deepCopy) {
        NodeRef copyRef = this.copyService.copyAndRename(c.alfresco(source),
                c.alfresco(destination),
                ContentModel.ASSOC_CONTAINS,
                null, deepCopy);
        return c.alfredApi(copyRef);
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef moveNode(eu.xenit.alfred.api.data.NodeRef source,
            eu.xenit.alfred.api.data.NodeRef destination) {
        ChildAssociationRef primaryParentAssoc = this.nodeService.getPrimaryParent(c.alfresco(source));
        ChildAssociationRef childAssoc = this.nodeService.moveNode(c.alfresco(source),
                c.alfresco(destination),
                ContentModel.ASSOC_CONTAINS,
                primaryParentAssoc.getQName());
        return c.alfredApi(childAssoc.getChildRef());
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef createNode(eu.xenit.alfred.api.data.NodeRef parent, String name,
            eu.xenit.alfred.api.data.QName type) {
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
        return c.alfredApi(createdNode.getNodeRef());
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef createNode(eu.xenit.alfred.api.data.NodeRef parent,
            Map<eu.xenit.alfred.api.data.QName, String[]> properties, eu.xenit.alfred.api.data.QName type,
            eu.xenit.alfred.api.data.ContentData contentData) {
        return createNode(parent, properties, null, null, type, contentData);
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef createNode(eu.xenit.alfred.api.data.NodeRef parent,
            Map<eu.xenit.alfred.api.data.QName, String[]> properties, eu.xenit.alfred.api.data.QName[] aspectsToAdd,
            eu.xenit.alfred.api.data.QName[] aspectsToRemove, eu.xenit.alfred.api.data.QName type,
            eu.xenit.alfred.api.data.ContentData contentData) {
        String[] names = properties.get(c.alfredApi(ContentModel.PROP_NAME));
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

            MetadataChanges aspects = new MetadataChanges();
            aspects.setAspectsToAdd(aspectsToAdd);
            aspects.setAspectsToRemove(aspectsToRemove);
            setMetadata(c.alfredApi(result.getChildRef()), aspects);

            if (contentData != null) {
                this.nodeService.setProperty(result.getChildRef(), ContentModel.PROP_CONTENT, c.alfresco(contentData));
            }
        } catch (DuplicateChildNodeNameException e) {
            throw new InvalidArgumentException("Node with same name already exists in  parent!");
        }

        return c.alfredApi(result.getChildRef());

    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef createNode(eu.xenit.alfred.api.data.NodeRef parent, String fileName,
            String contentType, MetadataChanges metadata, InputStream content) {
        eu.xenit.alfred.api.data.NodeRef resultNode = createNode(parent, fileName,
                new eu.xenit.alfred.api.data.QName(contentType));
        setContent(resultNode, content, fileName);
        if (metadata != null) {
            setMetadata(resultNode, metadata);
        }
        extractMetadata(resultNode);
        return resultNode;
    }

    @Override
    public void setContent(eu.xenit.alfred.api.data.NodeRef node, InputStream inputStream, String originalFilename) {
        if (inputStream == null) {
            nodeService.removeProperty(c.alfresco(node), ContentModel.PROP_CONTENT);
            return;
        }
        try {
            org.alfresco.service.cmr.repository.NodeRef createdNodeRef = c.alfresco(node);
            ContentWriter writer = contentService.getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);
            //guessMimetype places a ContentStreamListener on ContentWriter and waits for the input to be written.
            //Afterwards, makes a mime type guess based on file extension and on content.
            writer.guessMimetype(originalFilename);
            writer.putContent(inputStream);
            ContentData contentData = (ContentData) nodeService.getProperty(createdNodeRef, ContentModel.PROP_CONTENT);
            nodeService.setProperty(createdNodeRef, ContentModel.PROP_CONTENT, contentData);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void setContent(eu.xenit.alfred.api.data.NodeRef node, eu.xenit.alfred.api.data.ContentData contentData) {
        ContentData alfContentData = new ContentData(
                contentData.getContentUrl(),
                contentData.getMimetype(),
                contentData.getSize(),
                contentData.getEncoding(),
                contentData.getLocale());
        this.nodeService.setProperty(c.alfresco(node), ContentModel.PROP_CONTENT, alfContentData);
    }

    @Override
    public ContentInputStream getContent(eu.xenit.alfred.api.data.NodeRef nodeRef) {
        final ContentReader reader;
        try {
            reader = contentService.getReader(c.alfresco(nodeRef), ContentModel.PROP_CONTENT);
        } catch (InvalidNodeRefException invalidNoderefException) {
            logger.warn("Noderef {} is invalid. Noderef might be malformed or node does not exist.", nodeRef);
            return null;
        } catch (InvalidTypeException invalidTypeException) {
            logger.warn("Noderef {} is not of type 'Content'. Cannot return contentReader", nodeRef);
            return null;
        }
        if (reader == null) {
            return null;
        }
        InputStream inputContentStream = new DelayedInputStream(reader);
        return new ContentInputStream(inputContentStream, reader.getMimetype(), reader.getSize(),
                reader.getEncoding(), reader.getLocale());
    }

    @Override
    public eu.xenit.alfred.api.data.ContentData createContent(InputStream inputStream, String mimeType,
            String encoding) {
        try {
            ContentWriter writer = this.contentService.getWriter(null, ContentModel.PROP_CONTENT, false);
            writer.setMimetype(mimeType);
            writer.setEncoding(encoding);
            writer.putContent(inputStream);
            eu.xenit.alfred.api.data.ContentData result = new eu.xenit.alfred.api.data.ContentData(
                    writer.getContentUrl(),
                    writer.getMimetype(), writer.getSize(), writer.getEncoding(), writer.getLocale());
            return result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public eu.xenit.alfred.api.data.ContentData createContentWithMimetypeGuess(InputStream inputStream, String fileName,
            String encoding) {
        try {
            ContentWriter writer = contentService.getWriter(null, ContentModel.PROP_CONTENT, false);
            if (writer instanceof MimetypeServiceAware) {
                ((MimetypeServiceAware) writer).setMimetypeService(mimetypeService);
            }
            //guessMimetype places a ContentStreamListener on ContentWriter and waits for the input to be written.
            //Afterwards, makes a mime type guess based on file extension and on content.
            writer.guessMimetype(fileName);
            writer.putContent(inputStream);
            writer.setEncoding(encoding);
            return new eu.xenit.alfred.api.data.ContentData(
                    writer.getContentUrl(),
                    writer.getMimetype(),
                    writer.getSize(),
                    encoding,
                    writer.getLocale());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public boolean deleteNode(eu.xenit.alfred.api.data.NodeRef nodeRef) {
        return this.deleteNode(nodeRef, false);
    }

    @Override
    public boolean exists(eu.xenit.alfred.api.data.NodeRef nodeRef) {
        return nodeService.exists(c.alfresco(nodeRef));
    }

    @Override
    public boolean deleteNode(eu.xenit.alfred.api.data.NodeRef nodeRef, boolean permanently) {
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
    public eu.xenit.alfred.api.data.NodeRef checkout(eu.xenit.alfred.api.data.NodeRef original,
            eu.xenit.alfred.api.data.NodeRef destination) {
        eu.xenit.alfred.api.data.NodeRef workingCopy;

        LockStatus lockStatus = this.lockService.getLockStatus(c.alfresco(original));
        if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER) {
            logger.debug("Trying to checkout a working copy but this is not possible.");
            return null;
        }

        if (destination == null) {
            workingCopy = c.alfredApi(this.checkoutCheckinService.checkout(c.alfresco(original)));
        } else {
            ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(c.alfresco(destination));
            workingCopy = c.alfredApi(this.checkoutCheckinService.checkout(c.alfresco(original),
                    c.alfresco(destination),
                    ContentModel.ASSOC_CONTAINS,
                    childAssocRef.getQName()));
        }

        return workingCopy;
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef checkin(eu.xenit.alfred.api.data.NodeRef nodeRef, String comment,
            boolean majorVersion) {
        HashMap props = new HashMap(2, 1.0F);
        props.put("description", comment);
        props.put("versionType", majorVersion ? VersionType.MAJOR : VersionType.MINOR);

        eu.xenit.alfred.api.data.NodeRef original = c.alfredApi(
                this.checkoutCheckinService.checkin(c.alfresco(nodeRef), props));

        return original;
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef cancelCheckout(eu.xenit.alfred.api.data.NodeRef workingCopyRef) {
        NodeRef original = this.checkoutCheckinService.cancelCheckout(c.alfresco(workingCopyRef));
        return c.alfredApi(original);
    }

    @Override
    public eu.xenit.alfred.api.data.NodeRef getWorkingCopySource(eu.xenit.alfred.api.data.NodeRef workingCopyRef) {
        NodeRef originalRef = this.checkoutCheckinService.getCheckedOut(c.alfresco(workingCopyRef));
        return c.alfredApi(originalRef);
    }

    @Override
    public void extractMetadata(eu.xenit.alfred.api.data.NodeRef node) {
        org.alfresco.service.cmr.repository.NodeRef alfrescoNodeRef = c.alfresco(node);
        logger.debug("Extracting metadata for {}", alfrescoNodeRef);
        serviceRegistry.getActionService()
                .executeAction(serviceRegistry.getActionService().createAction("extract-metadata"), alfrescoNodeRef);
    }
}
