package eu.xenit.apix.tests.metadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Test;

public class AncestorsFromNodeUnitTest extends AncestorsBaseUnitTest {

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

    @Test
    public void getAncestorsOfNodeTest() {
        PermissionService alfrescoPermissionService = serviceRegistry.getPermissionService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();

        eu.xenit.apix.alfresco.metadata.NodeService apixNodeService = new eu.xenit.apix.alfresco.metadata.NodeService(
                serviceRegistry, apixAlfrescoConverter);
        eu.xenit.apix.data.NodeRef rootRef = new eu.xenit.apix.data.NodeRef(testNode3.toString());
        eu.xenit.apix.data.NodeRef testNode = new eu.xenit.apix.data.NodeRef(testNode1.toString());
        List<eu.xenit.apix.data.NodeRef> ancestors = apixNodeService.getAncestors(testNode, rootRef);
        Assert.assertEquals(2, ancestors.size());
        Assert.assertEquals(testNode2.toString(), ancestors.get(0).toString());
        Assert.assertEquals(testNode3.toString(), ancestors.get(1).toString());
        verify(alfrescoPermissionService, times(1)).hasPermission(eq(testNode1), eq(PermissionService.READ_PERMISSIONS));
        verify(alfrescoPermissionService, times(1)).hasPermission(eq(testNode2), eq(PermissionService.READ_PERMISSIONS));
        verify(alfrescoPermissionService, times(0)).hasPermission(eq(testNode3), eq(PermissionService.READ_PERMISSIONS));
        verify(alfrescoNodeService, times(1)).exists(eq(testNode1));
        verify(alfrescoNodeService, times(0)).exists(eq(testNode2));
        verify(alfrescoNodeService, times(0)).exists(eq(testNode3));
        verify(alfrescoNodeService, times(1)).getPrimaryParent(eq(testNode1));
        verify(alfrescoNodeService, times(1)).getPrimaryParent(eq(testNode2));
        verify(alfrescoNodeService, times(0)).getPrimaryParent(eq(testNode3));
    }
}
