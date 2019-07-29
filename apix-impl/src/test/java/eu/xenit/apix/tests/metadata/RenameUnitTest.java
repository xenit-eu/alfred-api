package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.MetadataChanges;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

public class RenameUnitTest {
    private ServiceRegistry serviceRegistry;
    private ApixToAlfrescoConversion apixAlfrescoConverter;

    private static final String NODEREF_STRING = "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bce";
    private static final String NEW_NAME = "newName";

    @Before
    public void init() {
        //Initializing node service
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.getType(any(org.alfresco.service.cmr.repository.NodeRef.class))).thenReturn(ContentModel.TYPE_CONTENT);

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
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistry);
    }

    @Test
    public void testSetNameCallsRenameOfFileFolderService(){
        FileFolderService alfrescoFileFolderService = serviceRegistry.getFileFolderService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();

        NodeRef nodeRef = new NodeRef(NODEREF_STRING);
        org.alfresco.service.cmr.repository.NodeRef alfrescoNodeRef = new org.alfresco.service.cmr.repository.NodeRef(nodeRef.toString());
        QName[] aspectsToAdd = new QName[0];
        QName[] aspectsToRemove = new QName[0];
        Map<QName, String[]> propertiesToSet = new HashMap<QName, String[]>();
        QName nameProperty = new QName(ContentModel.PROP_NAME.toString());
        String[] namePropertyValue = {NEW_NAME};
        propertiesToSet.put(nameProperty, namePropertyValue);
        MetadataChanges metadataChanges = new MetadataChanges(null, aspectsToAdd, aspectsToRemove, propertiesToSet);

        eu.xenit.apix.alfresco.metadata.NodeService apixNodeService = new eu.xenit.apix.alfresco.metadata.NodeService(serviceRegistry, apixAlfrescoConverter);
        apixNodeService.setMetadata(nodeRef, metadataChanges);
        try {
            verify(alfrescoFileFolderService, times(1))
                    .rename(eq(alfrescoNodeRef), eq(namePropertyValue[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        
    }
}
