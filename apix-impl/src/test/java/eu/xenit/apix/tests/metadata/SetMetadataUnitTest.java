package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.TypeService;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.dictionary.types.TypeDefinition;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.tests.helperClasses.alfresco.entities.Node;
import eu.xenit.apix.tests.helperClasses.apix.ApixTypeDefinitionStub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SetMetadataUnitTest {
    private static final String BASE_TYPE = "{http://www.alfresco.org/model/system/1.0}base";
    private static final String GRAND_PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type3";
    private static final String PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type2";
    private static final String INITIAL_TYPE = "{http://www.alfresco.org/model/content/1.0}type1";

    private static final String ASPECT1 = "{http://www.alfresco.org/model/content/1.0}aspect1";
    private static final String ASPECT2 = "{http://www.alfresco.org/model/content/1.0}aspect2";
    private static final String ASPECT3 = "{http://www.alfresco.org/model/content/1.0}aspect3";
    private static final String ASPECT4 = "{http://www.alfresco.org/model/content/1.0}aspect4";

    private NodeService nodeService;
    private TypeService typeService;
    private ApixToAlfrescoConversion apixAlfrescoConverter;
    private Node testNode;
    private QName aspect1;
    private QName aspect2;
    private QName aspect3;
    private QName aspect4;

    private ServiceRegistry serviceRegistryMock;

    @Before
    public void init() {
        initMocks();
    }

    private void initMocks() {
        //Initialization of type definitions
//        QName grandParentTypeQName = new QName(GRAND_PARENT_TYPE);
//        TypeDefinition grandParentTypeDefinition = createTypeDefinition(grandParentTypeQName,
//                new QName(BASE_TYPE),
//                new ArrayList<QName>());
//
//        QName parentTypeQName = new QName(PARENT_TYPE);
//        ArrayList<QName> aspects = new ArrayList<>();
//        aspects.add(new QName(ASPECT2));
//        TypeDefinition parentTypeDefinition = createTypeDefinition(parentTypeQName, grandParentTypeQName, aspects);
//
//        QName initialTypeQName = new QName(INITIAL_TYPE);
//        aspects = new ArrayList<>();
//        aspects.add(new QName(ASPECT1));
//        aspects.add(new QName(ASPECT2));
//        TypeDefinition initialTypeDefinition = createTypeDefinition(initialTypeQName, parentTypeQName, aspects);
//
//        Map<QName, TypeDefinition> typeDefMap = new HashMap<>();
//        typeDefMap.put(grandParentTypeQName, grandParentTypeDefinition);
//        typeDefMap.put(parentTypeQName, parentTypeDefinition);
//        typeDefMap.put(initialTypeQName, initialTypeDefinition);

        //Initialization of typeService
//        typeService = mock(TypeService.class);
//        Mockito.when(typeService.GetTypeDefinition(grandParentTypeQName)).thenReturn(grandParentTypeDefinition);
//        Mockito.when(typeService.GetTypeDefinition(parentTypeQName)).thenReturn(parentTypeDefinition);
//        Mockito.when(typeService.GetTypeDefinition(initialTypeQName)).thenReturn(initialTypeDefinition);

        //Initialization of aspect definition
//        Map<QName, AspectDefinition> aspectDefMap = initAspectDefinitions();

        //Initialization of test node
        testNode = generateTestNode();

        //Initialization of serviceRegistry and apixAlfrescoConverter
        serviceRegistryMock = mock(ServiceRegistry.class);
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistryMock);
//        Map<NodeRef, Node> testNodes = new HashMap<>();
//        testNodes.put(testNode.getNodeRef(), testNode);

        //Creating NodeService mocks
        org.alfresco.service.cmr.repository.NodeService nodeServiceMock = mock(
                org.alfresco.service.cmr.repository.NodeService.class);



        when(nodeServiceMock.exists(any(org.alfresco.service.cmr.repository.NodeRef.class))).thenReturn(true);
        when(nodeServiceMock.getType(any(org.alfresco.service.cmr.repository.NodeRef.class))).thenReturn(
                org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE));
        HashSet<org.alfresco.service.namespace.QName> testNodeAspects = new HashSet<>();
        testNodeAspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT1));
        testNodeAspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT2));
        testNodeAspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT3));
        when(nodeServiceMock.getAspects(any(org.alfresco.service.cmr.repository.NodeRef.class))).thenReturn(testNodeAspects);
        when(nodeServiceMock.getProperties(any(org.alfresco.service.cmr.repository.NodeRef.class))).thenReturn(new HashMap<>());
//        verify(nodeServiceMock, times(1)).setType(any(org.alfresco.service.cmr.repository.NodeRef.class), any(
//                org.alfresco.service.namespace.QName.class));
//        doAnswer((invocation) -> {
//            org.alfresco.service.cmr.repository.NodeRef nodeRef = (org.alfresco.service.cmr.repository.NodeRef) invocation.getArgument(0);
//            org.alfresco.service.namespace.QName newType = invocation.getArgument(1);
//            nodeRef.setType(newType);
//            return null;
//        }).when(nodeServiceMock).setType(any(org.alfresco.service.cmr.repository.NodeRef.class), eq(org.alfresco.service.namespace.QName.createQName(PARENT_TYPE)));

        Mockito.when(serviceRegistryMock.getNodeService())
                .thenReturn(nodeServiceMock);

        //Creating DictionaryService mock
        org.alfresco.service.cmr.dictionary.TypeDefinition typeDef1Mock = mock(
                org.alfresco.service.cmr.dictionary.TypeDefinition.class);
        when(typeDef1Mock.getName()).thenReturn(org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE));
        when(typeDef1Mock.getParentName()).thenReturn(org.alfresco.service.namespace.QName.createQName(PARENT_TYPE));
        when(typeDef1Mock.getDescription(any(MessageLookup.class))).thenReturn("");
        when(typeDef1Mock.getTitle(any(MessageLookup.class))).thenReturn("");
        when(typeDef1Mock.getProperties()).thenReturn(new HashMap<>());

        org.alfresco.service.cmr.dictionary.TypeDefinition typeDef2Mock = mock(
                org.alfresco.service.cmr.dictionary.TypeDefinition.class);
        when(typeDef2Mock.getName()).thenReturn(org.alfresco.service.namespace.QName.createQName(PARENT_TYPE));
        when(typeDef2Mock.getParentName()).thenReturn(org.alfresco.service.namespace.QName.createQName(GRAND_PARENT_TYPE));
        when(typeDef2Mock.getDescription(any(MessageLookup.class))).thenReturn("");
        when(typeDef2Mock.getTitle(any(MessageLookup.class))).thenReturn("");
        when(typeDef2Mock.getProperties()).thenReturn(new HashMap<>());

        org.alfresco.service.cmr.dictionary.TypeDefinition typeDef3Mock = mock(
                org.alfresco.service.cmr.dictionary.TypeDefinition.class);
        when(typeDef3Mock.getName()).thenReturn(org.alfresco.service.namespace.QName.createQName(GRAND_PARENT_TYPE));
        when(typeDef3Mock.getParentName()).thenReturn(org.alfresco.service.namespace.QName.createQName(BASE_TYPE));
        when(typeDef3Mock.getDescription(any(MessageLookup.class))).thenReturn("");
        when(typeDef3Mock.getTitle(any(MessageLookup.class))).thenReturn("");
        when(typeDef3Mock.getProperties()).thenReturn(new HashMap<>());

//        org.alfresco.service.cmr.dictionary.TypeDefinition typeDef4Mock = mock(
//                org.alfresco.service.cmr.dictionary.TypeDefinition.class);
//        when(typeDef4Mock.getName()).thenReturn(org.alfresco.service.namespace.QName.createQName(BASE_TYPE));
//        when(typeDef4Mock.getParentName()).thenReturn(null);
//        when(typeDef4Mock.getDescription(any(MessageLookup.class))).thenReturn("");
//        when(typeDef4Mock.getTitle(any(MessageLookup.class))).thenReturn("");
//        when(typeDef4Mock.getProperties()).thenReturn(new HashMap<>());

        DictionaryService dictionaryServiceMock = mock(DictionaryService.class);
        when(dictionaryServiceMock.isSubClass(eq(org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE)), eq(
                org.alfresco.service.namespace.QName.createQName(PARENT_TYPE)))).thenReturn(true);
        when(dictionaryServiceMock.getType(org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE))).thenReturn(typeDef1Mock);
        when(dictionaryServiceMock.getType(org.alfresco.service.namespace.QName.createQName(PARENT_TYPE))).thenReturn(typeDef2Mock);
        when(dictionaryServiceMock.getType(org.alfresco.service.namespace.QName.createQName(GRAND_PARENT_TYPE))).thenReturn(typeDef3Mock);
//        when(dictionaryServiceMock.getType(org.alfresco.service.namespace.QName.createQName(BASE_TYPE))).thenReturn(typeDef4Mock);

        Mockito.when(serviceRegistryMock.getDictionaryService()).thenReturn(dictionaryServiceMock);

        //Initialization of nodeService
        nodeService = new NodeService(serviceRegistryMock, apixAlfrescoConverter);
    }

//    private TypeDefinition createTypeDefinition(QName qName, QName parentQName, List<QName> aspects) {
//        ApixTypeDefinitionStub typeDef = new ApixTypeDefinitionStub();
//        typeDef.setName(qName);
//        typeDef.setParent(parentQName);
//        typeDef.setAspects(aspects);
//
//        return typeDef;
//    }

//    private Map<QName, AspectDefinition> initAspectDefinitions() {
//        aspect1 = new QName(ASPECT1);
//        AspectDefinition aspectDefinition1 = new AspectDefinition();
//        aspectDefinition1.setName(aspect1);
//
//        aspect2 = new QName(ASPECT2);
//        AspectDefinition aspectDefinition2 = new AspectDefinition();
//        aspectDefinition2.setName(aspect2);
//
//        aspect3 = new QName(ASPECT3);
//        AspectDefinition aspectDefinition3 = new AspectDefinition();
//        aspectDefinition3.setName(aspect3);
//
//        aspect4 = new QName(ASPECT4);
//        AspectDefinition aspectDefinition4 = new AspectDefinition();
//        aspectDefinition4.setName(aspect4);
//
//        Map<QName, AspectDefinition> aspectDefMap = new HashMap<>();
//        aspectDefMap.put(aspect1, aspectDefinition1);
//        aspectDefMap.put(aspect2, aspectDefinition2);
//        aspectDefMap.put(aspect3, aspectDefinition3);
//        aspectDefMap.put(aspect4, aspectDefinition4);
//
//        return aspectDefMap;
//    }

    private Node generateTestNode() {
        org.alfresco.service.cmr.repository.NodeRef nodeRef = new org.alfresco.service.cmr.repository.NodeRef("workspace://SpacesStore/00000000-0000-0000-0000-000000000000");

        //aspects
        List<org.alfresco.service.namespace.QName> aspects = new ArrayList<>();
        aspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT1));
        aspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT2));
        aspects.add(org.alfresco.service.namespace.QName.createQName(ASPECT3));

        //properties
        Map<org.alfresco.service.namespace.QName, String[]> properties = new HashMap<>();

        //type
        org.alfresco.service.namespace.QName type = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);

        return createNode(nodeRef, aspects, properties, type, 1);
    }

    private Node createNode(org.alfresco.service.cmr.repository.NodeRef nodeRef,
                            List<org.alfresco.service.namespace.QName> aspects,
                            Map<org.alfresco.service.namespace.QName, String[]> properties,
                            org.alfresco.service.namespace.QName type,
                            long transactionId) {
        return new Node(nodeRef, properties, aspects, type, transactionId);
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabled() {
        //Getting initial type
        org.alfresco.service.namespace.QName initialType = testNode.getType();
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
//        typeService = new TypeService(dictionaryService, apixAlfrescoConverter);
//        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);
        org.alfresco.service.cmr.dictionary.TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        //Choosing target type
        org.alfresco.service.namespace.QName targetType = intialTypeDef.getParentName();

        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
        Set<org.alfresco.service.namespace.QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add( targetType);

        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, null, null, null);

        //Getting the initial node type and aspects
        Set<org.alfresco.service.cmr.repository.NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNode.getNodeRef());
        NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator().next();
        NodeMetadata initialNodeMetadata = nodeService.getMetadata(apixTestNodeRef);
//        QName initialNodeType = initialNodeMetadata.type;
//        List<QName> initialNodeAspects = initialNodeMetadata.aspects;
//
//        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
//        Assert.assertEquals(3, initialNodeAspects.size());
//        Assert.assertTrue(initialNodeAspects.contains(new QName(ASPECT1)));
//        Assert.assertTrue(initialNodeAspects.contains(new QName(ASPECT2)));
//        Assert.assertTrue(initialNodeAspects.contains(new QName(ASPECT3)));

        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
        //After generalizing the type we expect:
        //- aspect1 is removed because it is part of the initial type definition but it no long part of the target type definition
        //- aspect2 still exists because it is part of the initial type definition and the target type definition
        //- aspect3 still exists because it isn't part of either type definition
        NodeService nodeServiceSpy = spy(nodeService);
        NodeMetadata finalNodeMetadata = nodeServiceSpy.setMetadata(apixTestNodeRef, changes);
        verify(nodeServiceSpy.getNodeService()).setType(any(org.alfresco.service.cmr.repository.NodeRef.class), any(org.alfresco.service.namespace.QName.class));
        verify(nodeServiceSpy).cleanupAspects(any(), any(), any(), anyBoolean());

//        NodeService nodserv = mock(NodeService.class);
//        nodserv.setMetadata(apixTestNodeRef, changes);
//        verify(nodserv).cleanupAspects(any(NodeRef.class), any(QName.class), any(QName.class), anyBoolean());
//        verify(nodeService).cleanupAspects(any(NodeRef.class), any(QName.class), any(QName.class), anyBoolean());
//        QName finalNodeType = finalNodeMetadata.type;
//        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
//        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
//        Assert.assertEquals(2, finalNodeAspects.size());
//        Assert.assertFalse(finalNodeAspects.contains(aspect1));
//        Assert.assertTrue(finalNodeAspects.contains(aspect2));
//        Assert.assertTrue(finalNodeAspects.contains(aspect3));
    }

//    @Test
//    public void testGeneralizeTypeWithCleanUpEnabledAndAdditionalAspects() {
//        //Getting initial type
//        QName initialType = testNode.getType();
//        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);
//
//        //Choosing target type
//        QName targetType = initialTypeDef.getParent();
//
//        //Choosing aspects to add
//        QName[] aspectsToAdd = new QName[1];
//        aspectsToAdd[0] = aspect4;
//
//        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
//        MetadataChanges changes = new MetadataChanges(targetType, true, aspectsToAdd, null, null);
//
//        //Getting the initial node type and aspects
//        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
//        QName initialNodeType = initialNodeMetadata.type;
//        List<QName> initialNodeAspects = initialNodeMetadata.aspects;
//
//        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
//        Assert.assertEquals(3, initialNodeAspects.size());
//        Assert.assertTrue(initialNodeAspects.contains(aspect1));
//        Assert.assertTrue(initialNodeAspects.contains(aspect2));
//        Assert.assertTrue(initialNodeAspects.contains(aspect3));
//        Assert.assertFalse(initialNodeAspects.contains(aspect4));
//
//        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
//        //After generalizing the type we expect:
//        //- aspect1 is removed because it is part of the initial type definition but it no long part of the target type definition
//        //- aspect2 still exists because it is part of the initial type definition and the target type definition
//        //- aspect3 still exists because it isn't part of either type definition
//        //- aspect4 is added because it has to be added by the aspectsToAdd in the metadata changes
//        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
//        QName finalNodeType = finalNodeMetadata.type;
//        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
//        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
//        Assert.assertEquals(3, finalNodeAspects.size());
//        Assert.assertFalse(finalNodeAspects.contains(aspect1));
//        Assert.assertTrue(finalNodeAspects.contains(aspect2));
//        Assert.assertTrue(finalNodeAspects.contains(aspect3));
//        Assert.assertTrue(finalNodeAspects.contains(aspect4));
//    }
//
//    @Test
//    public void testGeneralizeTypeWithCleanUpEnabledAndAddingAspectToBeCleanedUp() {
//        //Getting initial type
//        QName initialType = testNode.getType();
//        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);
//
//        //Choosing target type
//        QName targetType = initialTypeDef.getParent();
//
//        //Choosing aspects to add
//        QName[] aspectsToAdd = new QName[1];
//        aspectsToAdd[0] = aspect1;
//
//        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
//        MetadataChanges changes = new MetadataChanges(targetType, true, aspectsToAdd, null, null);
//
//        //Getting the initial node type and aspects
//        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
//        QName initialNodeType = initialNodeMetadata.type;
//        List<QName> initialNodeAspects = initialNodeMetadata.aspects;
//
//        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
//        Assert.assertEquals(3, initialNodeAspects.size());
//        Assert.assertTrue(initialNodeAspects.contains(aspect1));
//        Assert.assertTrue(initialNodeAspects.contains(aspect2));
//        Assert.assertTrue(initialNodeAspects.contains(aspect3));
//
//        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
//        //After generalizing the type we expect:
//        //- aspect1 is removed because it is part of the initial type definition but it no longer part of the target type definition.
//        //  However, it is part of the aspectsToAdd parameter in the metadata change so it should be added again.
//        //- aspect2 still exists because it is part of the initial type definition and the target type definition
//        //- aspect3 still exists because it isn't part of either type definition
//        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
//        QName finalNodeType = finalNodeMetadata.type;
//        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
//        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
//        Assert.assertEquals(3, finalNodeAspects.size());
//        Assert.assertTrue(finalNodeAspects.contains(aspect1));
//        Assert.assertTrue(finalNodeAspects.contains(aspect2));
//        Assert.assertTrue(finalNodeAspects.contains(aspect3));
//    }
//
//    @Test
//    public void testGeneralizeTypeWithCleanUpDisabled() {
//        //Getting initial type
//        QName initialType = testNode.getType();
//        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);
//
//        //Choosing target type
//        QName targetType = initialTypeDef.getParent();
//
//        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
//        MetadataChanges changes = new MetadataChanges(targetType, false, null, null, null);
//
//        //Getting the initial node type and aspects
//        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
//        QName initialNodeType = initialNodeMetadata.type;
//        List<QName> initialNodeAspects = initialNodeMetadata.aspects;
//
//        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
//        Assert.assertEquals(3, initialNodeAspects.size());
//        Assert.assertTrue(initialNodeAspects.contains(aspect1));
//        Assert.assertTrue(initialNodeAspects.contains(aspect2));
//        Assert.assertTrue(initialNodeAspects.contains(aspect3));
//
//        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
//        //After generalizing the type we expect:
//        //- The test node still has aspect1, aspect2 and aspect3
//        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
//        QName finalNodeType = finalNodeMetadata.type;
//        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
//        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
//        Assert.assertEquals(3, finalNodeAspects.size());
//        Assert.assertTrue(finalNodeAspects.contains(aspect1));
//        Assert.assertTrue(finalNodeAspects.contains(aspect2));
//        Assert.assertTrue(finalNodeAspects.contains(aspect3));
//    }
}
