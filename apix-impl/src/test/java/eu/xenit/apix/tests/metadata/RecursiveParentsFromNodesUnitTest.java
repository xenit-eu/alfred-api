package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.data.NodeRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Test;

public class RecursiveParentsFromNodesUnitTest extends RecursiveParentsBaseUnitTest {

    protected org.alfresco.service.cmr.repository.NodeRef testNodeWithoutReadPermission = new org.alfresco.service.cmr.repository.NodeRef(
            "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bd1");

    @Override
    protected NodeService initNodeServiceMock() {
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.exists(eq(testNode1))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNode2))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNode3))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNodeWithoutReadPermission))).thenReturn(true);
        ChildAssociationRef childAssocRef1 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, testNode2, null,
                testNode1);
        ChildAssociationRef childAssocRef2 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                testNodeWithoutReadPermission,
                null,
                testNode2);
        ChildAssociationRef childAssocRef3 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, testNode3, null,
                testNodeWithoutReadPermission);
        ChildAssociationRef childAssocRef4 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, null,
                testNode3);
        when(nodeServiceMock.getPrimaryParent(eq(testNode1))).thenReturn(childAssocRef1);
        when(nodeServiceMock.getPrimaryParent(eq(testNode2))).thenReturn(childAssocRef2);
        when(nodeServiceMock.getPrimaryParent(eq(testNodeWithoutReadPermission))).thenReturn(childAssocRef3);
        when(nodeServiceMock.getPrimaryParent(eq(testNode3))).thenReturn(childAssocRef4);

        return nodeServiceMock;
    }

    protected PermissionService initPermissionServiceMock() {
        PermissionService permissionServiceMock = super.initPermissionServiceMock();
        when(permissionServiceMock.hasReadPermission(eq(testNodeWithoutReadPermission)))
                .thenReturn(AccessStatus.DENIED);

        return permissionServiceMock;
    }

    @Test
    public void getRecursiveParentsOfMultipleNodesTestWithUnreadableParent() {
        PermissionService alfrescoPermissionService = serviceRegistry.getPermissionService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();

        eu.xenit.apix.alfresco.metadata.NodeService apixNodeService = new eu.xenit.apix.alfresco.metadata.NodeService(
                serviceRegistry, apixAlfrescoConverter);
        eu.xenit.apix.data.NodeRef rootRef = new eu.xenit.apix.data.NodeRef(testNode3.toString());
        eu.xenit.apix.data.NodeRef testNode = new eu.xenit.apix.data.NodeRef(testNode1.toString());
        List<NodeRef> testNodes = new ArrayList<>();
        testNodes.add(testNode);
        Map<NodeRef, List<NodeRef>> recursiveParents = apixNodeService.getRecursiveParents(testNodes, rootRef);
        Assert.assertEquals(0, recursiveParents.size());
        verify(alfrescoPermissionService, times(1)).hasReadPermission(eq(testNode1));
        verify(alfrescoPermissionService, times(1)).hasReadPermission(eq(testNode2));
        verify(alfrescoPermissionService, times(1)).hasReadPermission(eq(testNodeWithoutReadPermission));
        verify(alfrescoPermissionService, times(0)).hasReadPermission(eq(testNode3));
        verify(alfrescoNodeService, times(1)).exists(eq(testNode1));
        verify(alfrescoNodeService, times(0)).exists(eq(testNode2));
        verify(alfrescoNodeService, times(0)).exists(eq(testNodeWithoutReadPermission));
        verify(alfrescoNodeService, times(0)).exists(eq(testNode3));
        verify(alfrescoNodeService, times(1)).getPrimaryParent(eq(testNode1));
        verify(alfrescoNodeService, times(1)).getPrimaryParent(eq(testNode2));
        verify(alfrescoNodeService, times(0)).getPrimaryParent(eq(testNodeWithoutReadPermission));
        verify(alfrescoNodeService, times(0)).getPrimaryParent(eq(testNode3));
    }
}
