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
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class SetMetadataUnitTest {
    private static final String BASE_TYPE = "{http://www.alfresco.org/model/system/1.0}base";
    private static final String GRAND_PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type3";
    private static final String PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type2";
    private static final String INITIAL_TYPE = "{http://www.alfresco.org/model/content/1.0}type1";

    private static final String ASPECT1 = "{http://www.alfresco.org/model/content/1.0}aspect1";
    private static final String ASPECT2 = "{http://www.alfresco.org/model/content/1.0}aspect2";
    private static final String ASPECT3 = "{http://www.alfresco.org/model/content/1.0}aspect3";
    private static final String ASPECT4 = "{http://www.alfresco.org/model/content/1.0}aspect4";

    private org.alfresco.service.cmr.repository.NodeRef testNodeRef = new org.alfresco.service.cmr.repository.NodeRef("workspace://SpacesStore/00000000-0000-0000-0000-000000000000");

    private NodeService nodeService;
    private ApixToAlfrescoConversion apixAlfrescoConverter;
    private ServiceRegistry serviceRegistryMock;

    @Before
    public void init() {
        initMocks();
    }

    private void initMocks() {

        //Initialization of serviceRegistry and apixAlfrescoConverter
        serviceRegistryMock = mock(ServiceRegistry.class);
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistryMock);

        //Creating NodeService mock
        org.alfresco.service.cmr.repository.NodeService nodeServiceMock = initNodeServiceMock();
        Mockito.when(serviceRegistryMock.getNodeService()).thenReturn(nodeServiceMock);

        //Creating DictionaryService mock
        DictionaryService dictionaryServiceMock = initDictionaryServiceMock();
        Mockito.when(serviceRegistryMock.getDictionaryService()).thenReturn(dictionaryServiceMock);

        //Initialization of nodeService
        nodeService = new NodeService(serviceRegistryMock, apixAlfrescoConverter);
    }

    private org.alfresco.service.cmr.repository.NodeService initNodeServiceMock(){
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
        doNothing().when(nodeServiceMock).removeAspect(any(org.alfresco.service.cmr.repository.NodeRef.class), any(
                org.alfresco.service.namespace.QName.class));

        return nodeServiceMock;
    }

    private DictionaryService initDictionaryServiceMock(){
        //Initialization of the aspect definitions
        Map<org.alfresco.service.namespace.QName, AspectDefinition> aspectDefinitions = initAspectDefinitions();

        org.alfresco.service.namespace.QName aspect1 = org.alfresco.service.namespace.QName.createQName(ASPECT1);
        org.alfresco.service.namespace.QName aspect2 = org.alfresco.service.namespace.QName.createQName(ASPECT2);
        org.alfresco.service.namespace.QName aspect3 = org.alfresco.service.namespace.QName.createQName(ASPECT3);
        org.alfresco.service.namespace.QName aspect4 = org.alfresco.service.namespace.QName.createQName(ASPECT4);

        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        org.alfresco.service.namespace.QName parentType = org.alfresco.service.namespace.QName.createQName(PARENT_TYPE);
        org.alfresco.service.namespace.QName grandParentType = org.alfresco.service.namespace.QName.createQName(GRAND_PARENT_TYPE);

        //Initialization of the type definitions
        Map<org.alfresco.service.namespace.QName, TypeDefinition> typeDefinitions = initTypeDefinitions(aspectDefinitions);

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

    private Map<org.alfresco.service.namespace.QName, AspectDefinition> initAspectDefinitions(){
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDefinition1Mock = createAspectDefinition(ASPECT1);
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDefinition2Mock = createAspectDefinition(ASPECT2);
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDefinition3Mock = createAspectDefinition(ASPECT3);
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDefinition4Mock = createAspectDefinition(ASPECT4);

        Map<org.alfresco.service.namespace.QName, AspectDefinition> aspectsMap = new HashMap<>();
        aspectsMap.put(org.alfresco.service.namespace.QName.createQName(ASPECT1), aspectDefinition1Mock);
        aspectsMap.put(org.alfresco.service.namespace.QName.createQName(ASPECT2), aspectDefinition2Mock);
        aspectsMap.put(org.alfresco.service.namespace.QName.createQName(ASPECT3), aspectDefinition3Mock);
        aspectsMap.put(org.alfresco.service.namespace.QName.createQName(ASPECT4), aspectDefinition4Mock);
        return aspectsMap;
    }

    private AspectDefinition createAspectDefinition(String qnameString){
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDefinitionMock = mock(
                org.alfresco.service.cmr.dictionary.AspectDefinition.class);
        when(aspectDefinitionMock.getProperties()).thenReturn(new HashMap<>());
        org.alfresco.service.namespace.QName aspect = org.alfresco.service.namespace.QName.createQName(qnameString);
        when(aspectDefinitionMock.getName()).thenReturn(aspect);

        return aspectDefinitionMock;
    }

    private Map<org.alfresco.service.namespace.QName, TypeDefinition> initTypeDefinitions(Map<org.alfresco.service.namespace.QName, AspectDefinition> aspectDefinitions){
        org.alfresco.service.namespace.QName aspect1 = org.alfresco.service.namespace.QName.createQName(ASPECT1);
        org.alfresco.service.namespace.QName aspect2 = org.alfresco.service.namespace.QName.createQName(ASPECT2);
        org.alfresco.service.namespace.QName aspect3 = org.alfresco.service.namespace.QName.createQName(ASPECT3);

        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        org.alfresco.service.namespace.QName parentType = org.alfresco.service.namespace.QName.createQName(PARENT_TYPE);
        org.alfresco.service.namespace.QName grandParentType = org.alfresco.service.namespace.QName.createQName(GRAND_PARENT_TYPE);
        org.alfresco.service.namespace.QName baseType = org.alfresco.service.namespace.QName.createQName(BASE_TYPE);

        Set<org.alfresco.service.namespace.QName> defaultAspectsOfInitialType = new HashSet<>();
        defaultAspectsOfInitialType.add(aspect1);
        defaultAspectsOfInitialType.add(aspect2);
        defaultAspectsOfInitialType.add(aspect3);
        org.alfresco.service.cmr.dictionary.TypeDefinition initialTypeDefMock = createTypeDefinition(initialType,
                                                                                                     parentType,
                                                                                                     defaultAspectsOfInitialType,
                                                                                                     aspectDefinitions);

        Set<org.alfresco.service.namespace.QName> defaultAspectsOfParentType = new HashSet<>();
        defaultAspectsOfParentType.add(aspect1);
        defaultAspectsOfParentType.add(aspect2);
        org.alfresco.service.cmr.dictionary.TypeDefinition parentTypeDefMock = createTypeDefinition(parentType,
                                                                                                    grandParentType,
                                                                                                    defaultAspectsOfParentType,
                                                                                                    aspectDefinitions);

        Set<org.alfresco.service.namespace.QName> defaultAspectsOfGrandParentType = new HashSet<>();
        defaultAspectsOfGrandParentType.add(aspect1);
        org.alfresco.service.cmr.dictionary.TypeDefinition grandParentTypeDefMock = createTypeDefinition(grandParentType,
                                                                                                         baseType,
                                                                                                         defaultAspectsOfGrandParentType,
                                                                                                         aspectDefinitions);

        Map<org.alfresco.service.namespace.QName, TypeDefinition> typeDefinitions = new HashMap<>();
        typeDefinitions.put(initialType, initialTypeDefMock);
        typeDefinitions.put(parentType, parentTypeDefMock);
        typeDefinitions.put(grandParentType, grandParentTypeDefMock);
        return typeDefinitions;
    }

    private TypeDefinition createTypeDefinition(org.alfresco.service.namespace.QName type,
                                                org.alfresco.service.namespace.QName parentType,
                                                Set<org.alfresco.service.namespace.QName> aspects,
                                                Map<org.alfresco.service.namespace.QName, AspectDefinition> aspectDefinitions){
        org.alfresco.service.cmr.dictionary.TypeDefinition typeDefMock = mock(
                org.alfresco.service.cmr.dictionary.TypeDefinition.class);
        when(typeDefMock.getName()).thenReturn(type);
        when(typeDefMock.getParentName()).thenReturn(parentType);
        when(typeDefMock.getDescription(any(MessageLookup.class))).thenReturn("");
        when(typeDefMock.getTitle(any(MessageLookup.class))).thenReturn("");
        when(typeDefMock.getProperties()).thenReturn(new HashMap<>());
        when(typeDefMock.getDefaultAspectNames()).thenReturn(aspects);
        List<org.alfresco.service.cmr.dictionary.AspectDefinition> aspectDefsOfTypeDefMock = new ArrayList<>();
        for (org.alfresco.service.namespace.QName aspect : aspects){
            AspectDefinition aspectDef = aspectDefinitions.get(aspect);
            aspectDefsOfTypeDefMock.add(aspectDef);
        }
        when(typeDefMock.getDefaultAspects(anyBoolean())).thenReturn(aspectDefsOfTypeDefMock);

        return typeDefMock;
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabled() {
        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        org.alfresco.service.cmr.dictionary.TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        org.alfresco.service.namespace.QName targetType = intialTypeDef.getParentName();
        Set<org.alfresco.service.namespace.QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add( targetType);

        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, null, null, null);
        Set<org.alfresco.service.cmr.repository.NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator().next();
        NodeService nodeServiceSpy = spy(nodeService);
        nodeServiceSpy.setMetadata(apixTestNodeRef, changes);
        verify(nodeServiceSpy.getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeServiceSpy).cleanupAspects(any(), any(), any());
        verify(nodeServiceSpy.getNodeService(), times(0)).addAspect(eq(testNodeRef), any(
                org.alfresco.service.namespace.QName.class), any(HashMap.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAdditionalAspects() {
        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        org.alfresco.service.cmr.dictionary.TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        org.alfresco.service.namespace.QName targetType = intialTypeDef.getParentName();
        Set<org.alfresco.service.namespace.QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add( targetType);
        QName[] aspectsToAdd = new QName[1];
        aspectsToAdd[0] = new QName(ASPECT4);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, aspectsToAdd, null, null);
        Set<org.alfresco.service.cmr.repository.NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator().next();
        nodeService.setMetadata(apixTestNodeRef, changes);

        verify(nodeService.getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeService.getNodeService()).removeAspect(eq(testNodeRef), eq(org.alfresco.service.namespace.QName.createQName(ASPECT3)));
        verify(nodeService.getServiceRegistry().getDictionaryService()).getAspect(eq(org.alfresco.service.namespace.QName.createQName(ASPECT4)));
        verify(nodeService.getNodeService()).addAspect(eq(testNodeRef), eq(org.alfresco.service.namespace.QName.createQName(ASPECT4)), any(Map.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpEnabledAndAddingAspectToBeCleanedUp() {
        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        org.alfresco.service.cmr.dictionary.TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        org.alfresco.service.namespace.QName targetType = intialTypeDef.getParentName();
        Set<org.alfresco.service.namespace.QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add( targetType);
        QName[] aspectsToAdd = new QName[1];
        aspectsToAdd[0] = new QName(ASPECT3);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , true, aspectsToAdd, null, null);
        Set<org.alfresco.service.cmr.repository.NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator().next();
        nodeService.setMetadata(apixTestNodeRef, changes);

        InOrder inOrder = inOrder(nodeService.getNodeService());
        inOrder.verify(nodeService.getNodeService()).setType(eq(testNodeRef), eq(targetType));
        inOrder.verify(nodeService.getNodeService()).removeAspect(eq(testNodeRef), eq(org.alfresco.service.namespace.QName.createQName(ASPECT3)));
        inOrder.verify(nodeService.getNodeService()).addAspect(eq(testNodeRef), eq(org.alfresco.service.namespace.QName.createQName(ASPECT3)), any(Map.class));
    }

    @Test
    public void testGeneralizeTypeWithCleanUpDisabled() {
        org.alfresco.service.namespace.QName initialType = org.alfresco.service.namespace.QName.createQName(INITIAL_TYPE);
        DictionaryService dictionaryService = serviceRegistryMock.getDictionaryService();
        org.alfresco.service.cmr.dictionary.TypeDefinition intialTypeDef = dictionaryService.getType(initialType);
        org.alfresco.service.namespace.QName targetType = intialTypeDef.getParentName();
        Set<org.alfresco.service.namespace.QName> targetTypeSet = new HashSet<>();
        targetTypeSet.add( targetType);
        MetadataChanges changes = new MetadataChanges(apixAlfrescoConverter.apixQNames(targetTypeSet).iterator().next()
                , false, null, null, null);
        Set<org.alfresco.service.cmr.repository.NodeRef> testNodeRefSet = new HashSet<>();
        testNodeRefSet.add(testNodeRef);
        NodeRef apixTestNodeRef = apixAlfrescoConverter.apixNodeRefs(testNodeRefSet).iterator().next();
        NodeService nodeServiceSpy = spy(nodeService);
        nodeServiceSpy.setMetadata(apixTestNodeRef, changes);
        verify(nodeServiceSpy.getNodeService()).setType(eq(testNodeRef), eq(targetType));
        verify(nodeServiceSpy, times(0)).cleanupAspects(any(), any(), any());
        verify(nodeServiceSpy.getNodeService(), times(0)).addAspect(eq(testNodeRef), any(
                org.alfresco.service.namespace.QName.class), any(HashMap.class));
    }
}
