package eu.xenit.alfred.api.tests.metadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.alfresco.ApixToAlfrescoConversion;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.jupiter.api.BeforeEach;

public abstract class AncestorsBaseUnitTest {

    protected ServiceRegistry serviceRegistry;
    protected ApixToAlfrescoConversion apixAlfrescoConverter;

    protected NodeRef testNode1 = new NodeRef("workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bce");
    protected NodeRef testNode2 = new NodeRef("workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bcf");
    protected NodeRef testNode3 = new NodeRef("workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bd0");

    @BeforeEach
    public void init() {
        //Initializing service mocks
        serviceRegistry = mock(ServiceRegistry.class);
        PermissionService permissionServiceMock = initPermissionServiceMock();
        when(serviceRegistry.getPermissionService()).thenReturn(permissionServiceMock);
        NodeService nodeServiceMock = initNodeServiceMock();
        when(serviceRegistry.getNodeService()).thenReturn(nodeServiceMock);

        //Initializing apixAlfrescoConverter
        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistry);
    }

    protected PermissionService initPermissionServiceMock() {
        PermissionService permissionServiceMock = mock(PermissionService.class);
        when(permissionServiceMock.hasPermission(eq(testNode1), eq(PermissionService.READ))).thenReturn(AccessStatus.ALLOWED);
        when(permissionServiceMock.hasPermission(eq(testNode2), eq(PermissionService.READ))).thenReturn(AccessStatus.ALLOWED);
        when(permissionServiceMock.hasPermission(eq(testNode3), eq(PermissionService.READ))).thenReturn(AccessStatus.ALLOWED);

        return permissionServiceMock;
    }

    protected abstract NodeService initNodeServiceMock();
}
