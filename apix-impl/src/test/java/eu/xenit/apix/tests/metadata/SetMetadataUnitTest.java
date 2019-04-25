package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.node.MetadataChanges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class SetMetadataUnitTest {

    private static final String BASE_TYPE = "{http://www.alfresco.org/model/system/1.0}base";
    private static final String GRAND_PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type3";
    private static final String PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type2";
    private static final String INITIAL_TYPE = "{http://www.alfresco.org/model/content/1.0}type1";

    private static final String ASPECT1 = "{http://www.alfresco.org/model/content/1.0}aspect1";
    private static final String ASPECT2 = "{http://www.alfresco.org/model/content/1.0}aspect2";
    private static final String ASPECT3 = "{http://www.alfresco.org/model/content/1.0}aspect3";
    private static final String ASPECT4 = "{http://www.alfresco.org/model/content/1.0}aspect4";

    private NodeRef testNodeRef = new NodeRef(
            "workspace://SpacesStore/00000000-0000-0000-0000-000000000000");

    private eu.xenit.apix.alfresco.metadata.NodeService nodeService;
    private ApixToAlfrescoConversion apixAlfrescoConverter;
    private ServiceRegistry serviceRegistryMock;

    @Before
    public void init() {
        //Initialization of serviceRegistry and apixAlfrescoConverter
        serviceRegistryMock = mock(ServiceRegistry.class);
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistryMock);

        //Creating NodeService mock
        NodeService nodeServiceMock = initNodeServiceMock();
        when(serviceRegistryMock.getNodeService()).thenReturn(nodeServiceMock);

        //Creating DictionaryService mock
        DictionaryService dictionaryServiceMock = initDictionaryServiceMock();
        when(serviceRegistryMock.getDictionaryService()).thenReturn(dictionaryServiceMock);

        //Initialization of nodeService
        nodeService = new eu.xenit.apix.alfresco.metadata.NodeService(serviceRegistryMock, apixAlfrescoConverter);
    }

    private NodeService initNodeServiceMock() {
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.exists(any(NodeRef.class))).thenReturn(true);
        when(nodeServiceMock.getType(any(NodeRef.class))).thenReturn(
                QName.createQName(INITIAL_TYPE));
        HashSet<QName> testNodeAspects = new HashSet<>();
        testNodeAspects.add(QName.createQName(ASPECT1));
        testNodeAspects.add(QName.createQName(ASPECT2));
        testNodeAspects.add(QName.createQName(ASPECT3));
        when(nodeServiceMock.getAspects(any(NodeRef.class)))
                .thenReturn(testNodeAspects);
        when(nodeServiceMock.getProperties(any(NodeRef.class)))
                .thenReturn(new HashMap<>());
        doNothing().when(nodeServiceMock).removeAspect(any(NodeRef.class), any(
                QName.class));

        return nodeServiceMock;
    }

    private DictionaryService initDictionaryServiceMock() {
        //Initialization of the aspect definitions
        Map<QName, AspectDefinition> aspectDefinitions = initAspectDefinitions();

        QName aspect1 = QName.createQName(ASPECT1);
        QName aspect2 = QName.createQName(ASPECT2);
        QName aspect3 = QName.createQName(ASPECT3);
        QName aspect4 = QName.createQName(ASPECT4);

        QName initialType = QName
                .createQName(INITIAL_TYPE);
        QName parentType = QName.createQName(PARENT_TYPE);
        QName grandParentType = QName
                .createQName(GRAND_PARENT_TYPE);

        //Initialization of the type definitions
        Map<QName, TypeDefinition> typeDefinitions = initTypeDefinitions(
                aspectDefinitions);

        DictionaryService dictionaryServiceMock = mock(DictionaryService.class);
        when(dictionaryServiceMock.isSubClass(eq(initialType), eq(parentType))).thenReturn(true);
        when(dictionaryServiceMock.getType(initialType)).thenReturn(typeDefinitions.get(initialType));
        when(dictionaryServiceMock.getType(parentType)).thenReturn(typeDefinitions.get(parentType));
        when(dictionaryServiceMock.getType(grandParentType)).thenReturn(typeDefinitions.get(grandParentType));

        when(dictionaryServiceMock.getAspect(eq(aspect1))).thenReturn(aspectDefinitions.get(aspect1));
        when(dictionaryServiceMock.getAspect(eq(aspect2))).thenReturn(aspectDefinitions.get(aspect2));
        when(dictionaryServiceMock.getAspect(eq(aspect3))).thenReturn(aspectDefinitions.get(aspect3));
        when(dictionaryServiceMock.getAspect(eq(aspect4))).thenReturn(aspectDefinitions.get(aspect4));

        return dictionaryServiceMock;
    }

    private Map<QName, AspectDefinition> initAspectDefinitions() {
        AspectDefinition aspectDefinition1Mock = createAspectDefinition(ASPECT1);
        AspectDefinition aspectDefinition2Mock = createAspectDefinition(ASPECT2);
        AspectDefinition aspectDefinition3Mock = createAspectDefinition(ASPECT3);
        AspectDefinition aspectDefinition4Mock = createAspectDefinition(ASPECT4);

        Map<QName, AspectDefinition> aspectsMap = new HashMap<>();
        aspectsMap.put(QName.createQName(ASPECT1), aspectDefinition1Mock);
        aspectsMap.put(QName.createQName(ASPECT2), aspectDefinition2Mock);
        aspectsMap.put(QName.createQName(ASPECT3), aspectDefinition3Mock);
        aspectsMap.put(QName.createQName(ASPECT4), aspectDefinition4Mock);
        return aspectsMap;
    }

    private AspectDefinition createAspectDefinition(String qnameString) {
        AspectDefinition aspectDefinitionMock = mock(AspectDefinition.class);
        when(aspectDefinitionMock.getProperties()).thenReturn(new HashMap<>());
        QName aspect = QName.createQName(qnameString);
        when(aspectDefinitionMock.getName()).thenReturn(aspect);

        return aspectDefinitionMock;
    }

    private Map<QName, TypeDefinition> initTypeDefinitions(
            Map<QName, AspectDefinition> aspectDefinitions) {
        QName aspect1 = QName.createQName(ASPECT1);
        QName aspect2 = QName.createQName(ASPECT2);
        QName aspect3 = QName.createQName(ASPECT3);

        QName initialType = QName
                .createQName(INITIAL_TYPE);
        QName parentType = QName.createQName(PARENT_TYPE);
        QName grandParentType = QName
                .createQName(GRAND_PARENT_TYPE);
        QName baseType = QName.createQName(BASE_TYPE);

        Set<QName> defaultAspectsOfInitialType = new HashSet<>();
        defaultAspectsOfInitialType.add(aspect1);
        defaultAspectsOfInitialType.add(aspect2);
        defaultAspectsOfInitialType.add(aspect3);
        TypeDefinition initialTypeDefMock = createTypeDefinition(initialType,
                parentType,
                defaultAspectsOfInitialType,
                aspectDefinitions);

        Set<QName> defaultAspectsOfParentType = new HashSet<>();
        defaultAspectsOfParentType.add(aspect1);
        defaultAspectsOfParentType.add(aspect2);
        TypeDefinition parentTypeDefMock = createTypeDefinition(parentType,
                grandParentType,
                defaultAspectsOfParentType,
                aspectDefinitions);

        Set<QName> defaultAspectsOfGrandParentType = new HashSet<>();
        defaultAspectsOfGrandParentType.add(aspect1);
        TypeDefinition grandParentTypeDefMock = createTypeDefinition(
                grandParentType,
                baseType,
                defaultAspectsOfGrandParentType,
                aspectDefinitions);

        Map<QName, TypeDefinition> typeDefinitions = new HashMap<>();
        typeDefinitions.put(initialType, initialTypeDefMock);
        typeDefinitions.put(parentType, parentTypeDefMock);
        typeDefinitions.put(grandParentType, grandParentTypeDefMock);
        return typeDefinitions;
    }

    private TypeDefinition createTypeDefinition(QName type, QName parentType, Set<QName> aspects,
            Map<QName, AspectDefinition> aspectDefinitions) {TypeDefinition typeDefMock = mock(TypeDefinition.class);
        when(typeDefMock.getName()).thenReturn(type);
        when(typeDefMock.getParentName()).thenReturn(parentType);
        when(typeDefMock.getDescription(any(MessageLookup.class))).thenReturn("");
        when(typeDefMock.getTitle(any(MessageLookup.class))).thenReturn("");
        when(typeDefMock.getProperties()).thenReturn(new HashMap<>());
        when(typeDefMock.getDefaultAspectNames()).thenReturn(aspects);
        List<AspectDefinition> aspectDefsOfTypeDefMock = new ArrayList<>();
        for (QName aspect : aspects) {
            AspectDefinition aspectDef = aspectDefinitions.get(aspect);
            aspectDefsOfTypeDefMock.add(aspectDef);
        }
        when(typeDefMock.getDefaultAspects(anyBoolean())).thenReturn(aspectDefsOfTypeDefMock);

        return typeDefMock;
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabled() {
        QName initialType = QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        QName targetType = intialTypeDef.getParentName();
        Set<QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add(targetType);

        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, null, null, null);
        Set<NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        eu.xenit.apix.data.NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator()
                .next();
        eu.xenit.apix.alfresco.metadata.NodeService nodeServiceSpy = spy(nodeService);
        nodeServiceSpy.setMetadata(apixTestNodeRef, changes);
        verify(nodeServiceSpy.getServiceRegistry().getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeServiceSpy).cleanupAspects(any(), any(), any());
        verify(nodeServiceSpy.getServiceRegistry().getNodeService(), times(0))
                .addAspect(eq(testNodeRef), any(QName.class), any(HashMap.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAdditionalAspects() {
        QName initialType = QName
                .createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        QName targetType = intialTypeDef.getParentName();
        Set<QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add(targetType);
        eu.xenit.apix.data.QName[] aspectsToAdd = new eu.xenit.apix.data.QName[1];
        aspectsToAdd[0] = new eu.xenit.apix.data.QName(ASPECT4);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, aspectsToAdd, null, null);
        Set<NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        eu.xenit.apix.data.NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator()
                .next();
        nodeService.setMetadata(apixTestNodeRef, changes);

        verify(nodeService.getServiceRegistry().getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeService.getServiceRegistry().getNodeService())
                .removeAspect(eq(testNodeRef), eq(QName.createQName(ASPECT3)));
        verify(nodeService.getServiceRegistry().getDictionaryService())
                .getAspect(eq(QName.createQName(ASPECT4)));
        verify(nodeService.getServiceRegistry().getNodeService())
                .addAspect(eq(testNodeRef), eq(QName.createQName(ASPECT4)), any(Map.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAddingAspectToBeCleanedUp() {
        QName initialType = QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        QName targetType = intialTypeDef.getParentName();
        Set<QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add(targetType);
        eu.xenit.apix.data.QName[] aspectsToAdd = new eu.xenit.apix.data.QName[1];
        aspectsToAdd[0] = new eu.xenit.apix.data.QName(ASPECT3);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, aspectsToAdd, null, null);
        Set<NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        eu.xenit.apix.data.NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator()
                .next();
        nodeService.setMetadata(apixTestNodeRef, changes);

        InOrder inOrder = inOrder(nodeService.getServiceRegistry().getNodeService());
        inOrder.verify(nodeService.getServiceRegistry().getNodeService()).setType(eq(testNodeRef), eq(targetType));
        inOrder.verify(nodeService.getServiceRegistry().getNodeService())
                .removeAspect(eq(testNodeRef), eq(QName.createQName(ASPECT3)));
        inOrder.verify(nodeService.getServiceRegistry().getNodeService())
                .addAspect(eq(testNodeRef), eq(QName.createQName(ASPECT3)), any(Map.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpDisabled() {
        QName initialType = QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        QName targetType = intialTypeDef.getParentName();
        Set<QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add(targetType);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , false, null, null, null);
        Set<NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        eu.xenit.apix.data.NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator()
                .next();
        eu.xenit.apix.alfresco.metadata.NodeService nodeServiceSpy = spy(nodeService);
        nodeServiceSpy.setMetadata(apixTestNodeRef, changes);
        verify(nodeServiceSpy.getServiceRegistry().getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeServiceSpy, times(0)).cleanupAspects(any(), any(), any());
        verify(nodeServiceSpy.getServiceRegistry().getNodeService(), times(0))
                .addAspect(eq(testNodeRef), any(QName.class), any(HashMap.class));
    }
}
