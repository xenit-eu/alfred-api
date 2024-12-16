package eu.xenit.alfred.api.tests.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.node.MetadataChanges;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

public class RenameUnitTest {

    private ServiceRegistry serviceRegistry;
    private AlfredApiToAlfrescoConversion alfredApiAlfrescoConverter;

    private static final String NODEREF_STRING = "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bce";
    private static final String NEW_NAME = "newName";

    @BeforeEach
    public void init() {
        //Initializing node service
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.getType(any(org.alfresco.service.cmr.repository.NodeRef.class)))
                .thenReturn(ContentModel.TYPE_CONTENT);

        //Initializing file folder service
        FileFolderService fileFolderServiceMock = mock(FileFolderService.class);

        //Initializing dictionary service
        DictionaryService dictionaryServiceMock = mock(DictionaryService.class);
        when(dictionaryServiceMock.isSubClass(any(org.alfresco.service.namespace.QName.class),
                eq(ContentModel.TYPE_FOLDER))).thenReturn(false);
        when(dictionaryServiceMock.isSubClass(any(org.alfresco.service.namespace.QName.class),
                eq(ContentModel.TYPE_SYSTEM_FOLDER))).thenReturn(false);
        when(dictionaryServiceMock.isSubClass(any(org.alfresco.service.namespace.QName.class),
                eq(ContentModel.TYPE_CONTENT))).thenReturn(true);

        //Initializing service registry
        serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.getNodeService()).thenReturn(nodeServiceMock);
        when(serviceRegistry.getFileFolderService()).thenReturn(fileFolderServiceMock);
        when(serviceRegistry.getDictionaryService()).thenReturn(dictionaryServiceMock);

        //Initializing Alfred API to Alfresco converter
        alfredApiAlfrescoConverter = new AlfredApiToAlfrescoConversion(serviceRegistry);
    }

    @Test
    public void testSetNameCallsRenameOfFileFolderService() {
        FileFolderService alfrescoFileFolderService = serviceRegistry.getFileFolderService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();

        NodeRef nodeRef = new NodeRef(NODEREF_STRING);
        org.alfresco.service.cmr.repository.NodeRef alfrescoNodeRef = new org.alfresco.service.cmr.repository.NodeRef(
                nodeRef.toString());
        QName[] aspectsToAdd = new QName[0];
        QName[] aspectsToRemove = new QName[0];
        Map<QName, String[]> propertiesToSet = new HashMap<QName, String[]>();
        QName nameProperty = new QName(ContentModel.PROP_NAME.toString());
        String[] namePropertyValue = {NEW_NAME};
        propertiesToSet.put(nameProperty, namePropertyValue);
        MetadataChanges metadataChanges = new MetadataChanges(null, aspectsToAdd, aspectsToRemove, propertiesToSet);

        eu.xenit.alfred.api.alfresco.metadata.NodeService alfredApiNodeService = new eu.xenit.alfred.api.alfresco.metadata.NodeService(
                serviceRegistry, alfredApiAlfrescoConverter, null);
        alfredApiNodeService.setMetadata(nodeRef, metadataChanges);

        //We have to make sure that the rename method of the file folder service was called once.
        //That way we know that the qname path was also changed.
        try {
            verify(alfrescoFileFolderService, times(1))
                    .rename(eq(alfrescoNodeRef), eq(namePropertyValue[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //We also have to make sure that the name property was removed from the remaining properties to add.
        //Otherwise the name of the node will be changed twice and this can trigger a behaviour twice.
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(alfrescoNodeService, times(1)).addProperties(eq(alfrescoNodeRef), captor.capture());
        assert 0 == captor.getValue().size();
    }
}
