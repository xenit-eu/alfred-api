package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
public class AncestorsFromInaccessibleNodeUnitTest extends AncestorsBaseUnitTest {

    @Override
    protected NodeService initNodeServiceMock() {
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.exists(eq(testNode1))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNode2))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNode3))).thenReturn(true);
        ChildAssociationRef childAssocRef1 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, testNode2, null,
                testNode1);
        ChildAssociationRef childAssocRef2 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, testNode3, null,
                testNode2);
        ChildAssociationRef childAssocRef3 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, null,
                testNode3);
        when(nodeServiceMock.getPrimaryParent(eq(testNode1))).thenReturn(childAssocRef1);
        when(nodeServiceMock.getPrimaryParent(eq(testNode2))).thenReturn(childAssocRef2);
        when(nodeServiceMock.getPrimaryParent(eq(testNode3))).thenReturn(childAssocRef3);

        return nodeServiceMock;
    }

    protected PermissionService initPermissionServiceMock() {
        PermissionService permissionServiceMock = super.initPermissionServiceMock();
        when(permissionServiceMock.hasPermission(eq(testNode2), eq(PermissionService.READ)))
                .thenReturn(AccessStatus.DENIED);

        return permissionServiceMock;
    }

    @Test
    public void getAncestorsOfNodeTest() {
        Assertions.assertThrows(AccessDeniedException.class,
()->{
            eu.xenit.apix.alfresco.metadata.NodeService apixNodeService = new eu.xenit.apix.alfresco.metadata.NodeService(
                    serviceRegistry, apixAlfrescoConverter);
            eu.xenit.apix.data.NodeRef rootRef = new eu.xenit.apix.data.NodeRef(testNode3.toString());
            eu.xenit.apix.data.NodeRef testNode = new eu.xenit.apix.data.NodeRef(testNode1.toString());
            apixNodeService.getAncestors(testNode, rootRef);
        }, "Expected an AccessDeniedException to be thrown");
    }
}
