package eu.xenit.apix.tests.metadata;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.TypeService;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.dictionary.types.TypeDefinition;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.tests.helperClasses.alfresco.services.AlfrescoDictionaryServiceStub;
import eu.xenit.apix.tests.helperClasses.alfresco.services.AlfrescoNodeServiceStub;
import eu.xenit.apix.tests.helperClasses.alfresco.entities.Node;
import eu.xenit.apix.tests.helperClasses.apix.ApixTypeDefinitionStub;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.ServiceRegistry;
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

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        initMocks();
    }

    private void initMocks() {
        //Initialization of type definitions
        QName grandParentTypeQName = new QName(GRAND_PARENT_TYPE);
        TypeDefinition grandParentTypeDefinition = createTypeDefinition(grandParentTypeQName,
                new QName(BASE_TYPE),
                new ArrayList<QName>());

        QName parentTypeQName = new QName(PARENT_TYPE);
        ArrayList<QName> aspects = new ArrayList<>();
        aspects.add(new QName(ASPECT2));
        TypeDefinition parentTypeDefinition = createTypeDefinition(parentTypeQName, grandParentTypeQName, aspects);

        QName initialTypeQName = new QName(INITIAL_TYPE);
        aspects = new ArrayList<>();
        aspects.add(new QName(ASPECT1));
        aspects.add(new QName(ASPECT2));
        TypeDefinition initialTypeDefinition = createTypeDefinition(initialTypeQName, parentTypeQName, aspects);

        Map<QName, TypeDefinition> typeDefMap = new HashMap<>();
        typeDefMap.put(grandParentTypeQName, grandParentTypeDefinition);
        typeDefMap.put(parentTypeQName, parentTypeDefinition);
        typeDefMap.put(initialTypeQName, initialTypeDefinition);

        //Initialization of typeService
        typeService = Mockito.mock(TypeService.class);
        Mockito.when(typeService.GetTypeDefinition(grandParentTypeQName)).thenReturn(grandParentTypeDefinition);
        Mockito.when(typeService.GetTypeDefinition(parentTypeQName)).thenReturn(parentTypeDefinition);
        Mockito.when(typeService.GetTypeDefinition(initialTypeQName)).thenReturn(initialTypeDefinition);

        //Initialization of aspect definition
        Map<QName, AspectDefinition> aspectDefMap = initAspectDefinitions();

        //Initialization of test node
        testNode = generateTestNode();

        //Initialization of serviceRegistry and apixAlfrescoConverter
        ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistry);
        Map<NodeRef, Node> testNodes = new HashMap<>();
        testNodes.put(testNode.getNodeRef(), testNode);
        Mockito.when(serviceRegistry.getNodeService())
                .thenReturn(new AlfrescoNodeServiceStub(testNodes, apixAlfrescoConverter));
        Mockito.when(serviceRegistry.getDictionaryService()).thenReturn(new AlfrescoDictionaryServiceStub(aspectDefMap,
                new HashMap<QName, PropertyDefinition>(),
                typeDefMap,
                apixAlfrescoConverter));

        //Initialization of nodeService
        nodeService = new NodeService(serviceRegistry, apixAlfrescoConverter);
    }

    private TypeDefinition createTypeDefinition(QName qName, QName parentQName, List<QName> aspects) {
        ApixTypeDefinitionStub typeDef = new ApixTypeDefinitionStub();
        typeDef.setName(qName);
        typeDef.setParent(parentQName);
        typeDef.setAspects(aspects);

        return typeDef;
    }

    private Map<QName, AspectDefinition> initAspectDefinitions() {
        aspect1 = new QName(ASPECT1);
        AspectDefinition aspectDefinition1 = new AspectDefinition();
        aspectDefinition1.setName(aspect1);

        aspect2 = new QName(ASPECT2);
        AspectDefinition aspectDefinition2 = new AspectDefinition();
        aspectDefinition2.setName(aspect2);

        aspect3 = new QName(ASPECT3);
        AspectDefinition aspectDefinition3 = new AspectDefinition();
        aspectDefinition3.setName(aspect3);

        aspect4 = new QName(ASPECT4);
        AspectDefinition aspectDefinition4 = new AspectDefinition();
        aspectDefinition4.setName(aspect4);

        Map<QName, AspectDefinition> aspectDefMap = new HashMap<>();
        aspectDefMap.put(aspect1, aspectDefinition1);
        aspectDefMap.put(aspect2, aspectDefinition2);
        aspectDefMap.put(aspect3, aspectDefinition3);
        aspectDefMap.put(aspect4, aspectDefinition4);

        return aspectDefMap;
    }

    private Node generateTestNode() {
        NodeRef nodeRef = new NodeRef("workspace://SpacesStore/00000000-0000-0000-0000-000000000000");

        //aspects
        List<QName> aspects = new ArrayList<>();
        aspects.add(aspect1);
        aspects.add(aspect2);
        aspects.add(aspect3);

        //properties
        Map<QName, String[]> properties = new HashMap<>();

        //type
        QName type = new QName(INITIAL_TYPE);

        return createNode(nodeRef, aspects, properties, type, 1);
    }

    private Node createNode(NodeRef nodeRef, List<QName> aspects, Map<QName, String[]> properties, QName type,
            long transactionId) {
        return new Node(nodeRef, properties, aspects, type, transactionId);
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabled() {
        //Getting initial type
        QName initialType = testNode.getType();
        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);

        //Choosing target type
        QName targetType = initialTypeDef.getParent();

        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
        MetadataChanges changes = new MetadataChanges(targetType, true, null, null, null);

        //Getting the initial node type and aspects
        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
        QName initialNodeType = initialNodeMetadata.type;
        List<QName> initialNodeAspects = initialNodeMetadata.aspects;

        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
        Assert.assertEquals(3, initialNodeAspects.size());
        Assert.assertTrue(initialNodeAspects.contains(aspect1));
        Assert.assertTrue(initialNodeAspects.contains(aspect2));
        Assert.assertTrue(initialNodeAspects.contains(aspect3));

        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
        //After generalizing the type we expect:
        //- aspect1 is removed because it is part of the initial type definition but it no long part of the target type definition
        //- aspect2 still exists because it is part of the initial type definition and the target type definition
        //- aspect3 still exists because it isn't part of either type definition
        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
        QName finalNodeType = finalNodeMetadata.type;
        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
        Assert.assertEquals(2, finalNodeAspects.size());
        Assert.assertFalse(finalNodeAspects.contains(aspect1));
        Assert.assertTrue(finalNodeAspects.contains(aspect2));
        Assert.assertTrue(finalNodeAspects.contains(aspect3));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAdditionalAspects() {
        //Getting initial type
        QName initialType = testNode.getType();
        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);

        //Choosing target type
        QName targetType = initialTypeDef.getParent();

        //Choosing aspects to add
        QName[] aspectsToAdd = new QName[1];
        aspectsToAdd[0] = aspect4;

        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
        MetadataChanges changes = new MetadataChanges(targetType, true, aspectsToAdd, null, null);

        //Getting the initial node type and aspects
        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
        QName initialNodeType = initialNodeMetadata.type;
        List<QName> initialNodeAspects = initialNodeMetadata.aspects;

        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
        Assert.assertEquals(3, initialNodeAspects.size());
        Assert.assertTrue(initialNodeAspects.contains(aspect1));
        Assert.assertTrue(initialNodeAspects.contains(aspect2));
        Assert.assertTrue(initialNodeAspects.contains(aspect3));
        Assert.assertFalse(initialNodeAspects.contains(aspect4));

        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
        //After generalizing the type we expect:
        //- aspect1 is removed because it is part of the initial type definition but it no long part of the target type definition
        //- aspect2 still exists because it is part of the initial type definition and the target type definition
        //- aspect3 still exists because it isn't part of either type definition
        //- aspect4 is added because it has to be added by the aspectsToAdd in the metadata changes
        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
        QName finalNodeType = finalNodeMetadata.type;
        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
        Assert.assertEquals(3, finalNodeAspects.size());
        Assert.assertFalse(finalNodeAspects.contains(aspect1));
        Assert.assertTrue(finalNodeAspects.contains(aspect2));
        Assert.assertTrue(finalNodeAspects.contains(aspect3));
        Assert.assertTrue(finalNodeAspects.contains(aspect4));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAddingAspectToBeCleanedUp() {
        //Getting initial type
        QName initialType = testNode.getType();
        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);

        //Choosing target type
        QName targetType = initialTypeDef.getParent();

        //Choosing aspects to add
        QName[] aspectsToAdd = new QName[1];
        aspectsToAdd[0] = aspect1;

        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
        MetadataChanges changes = new MetadataChanges(targetType, true, aspectsToAdd, null, null);

        //Getting the initial node type and aspects
        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
        QName initialNodeType = initialNodeMetadata.type;
        List<QName> initialNodeAspects = initialNodeMetadata.aspects;

        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
        Assert.assertEquals(3, initialNodeAspects.size());
        Assert.assertTrue(initialNodeAspects.contains(aspect1));
        Assert.assertTrue(initialNodeAspects.contains(aspect2));
        Assert.assertTrue(initialNodeAspects.contains(aspect3));

        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
        //After generalizing the type we expect:
        //- aspect1 is removed because it is part of the initial type definition but it no longer part of the target type definition.
        //  However, it is part of the aspectsToAdd parameter in the metadata change so it should be added again.
        //- aspect2 still exists because it is part of the initial type definition and the target type definition
        //- aspect3 still exists because it isn't part of either type definition
        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
        QName finalNodeType = finalNodeMetadata.type;
        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
        Assert.assertEquals(3, finalNodeAspects.size());
        Assert.assertTrue(finalNodeAspects.contains(aspect1));
        Assert.assertTrue(finalNodeAspects.contains(aspect2));
        Assert.assertTrue(finalNodeAspects.contains(aspect3));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpDisabled() {
        //Getting initial type
        QName initialType = testNode.getType();
        TypeDefinition initialTypeDef = typeService.GetTypeDefinition(initialType);

        //Choosing target type
        QName targetType = initialTypeDef.getParent();

        //Setting the target type in the metadata changes and enabling clean-up of aspects on generalization
        MetadataChanges changes = new MetadataChanges(targetType, false, null, null, null);

        //Getting the initial node type and aspects
        NodeMetadata initialNodeMetadata = nodeService.getMetadata(testNode.getNodeRef());
        QName initialNodeType = initialNodeMetadata.type;
        List<QName> initialNodeAspects = initialNodeMetadata.aspects;

        Assert.assertEquals(new QName(INITIAL_TYPE), initialNodeType);
        Assert.assertEquals(3, initialNodeAspects.size());
        Assert.assertTrue(initialNodeAspects.contains(aspect1));
        Assert.assertTrue(initialNodeAspects.contains(aspect2));
        Assert.assertTrue(initialNodeAspects.contains(aspect3));

        //Setting the node type to the target type. We initially have 3 aspects (aspect1, aspect2 and aspect3).
        //After generalizing the type we expect:
        //- The test node still has aspect1, aspect2 and aspect3
        NodeMetadata finalNodeMetadata = nodeService.setMetadata(testNode.getNodeRef(), changes);
        QName finalNodeType = finalNodeMetadata.type;
        List<QName> finalNodeAspects = finalNodeMetadata.aspects;
        Assert.assertEquals(new QName(PARENT_TYPE), finalNodeType);
        Assert.assertEquals(3, finalNodeAspects.size());
        Assert.assertTrue(finalNodeAspects.contains(aspect1));
        Assert.assertTrue(finalNodeAspects.contains(aspect2));
        Assert.assertTrue(finalNodeAspects.contains(aspect3));
    }
}
