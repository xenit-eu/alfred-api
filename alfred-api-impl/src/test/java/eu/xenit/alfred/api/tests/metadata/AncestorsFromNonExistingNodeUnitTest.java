package eu.xenit.alfred.api.tests.metadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.NodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AncestorsFromNonExistingNodeUnitTest extends AncestorsBaseUnitTest {

    @Override
    protected NodeService initNodeServiceMock() {
        NodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.exists(eq(testNode1))).thenReturn(false);
        when(nodeServiceMock.exists(eq(testNode2))).thenReturn(true);
        when(nodeServiceMock.exists(eq(testNode3))).thenReturn(true);
        ChildAssociationRef childAssocRef2 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, testNode3, null,
                testNode2);
        ChildAssociationRef childAssocRef3 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, null,
                testNode3);
        when(nodeServiceMock.getPrimaryParent(eq(testNode1))).thenThrow(new InvalidNodeRefException(testNode1));
        when(nodeServiceMock.getPrimaryParent(eq(testNode2))).thenReturn(childAssocRef2);
        when(nodeServiceMock.getPrimaryParent(eq(testNode3))).thenReturn(childAssocRef3);

        return nodeServiceMock;
    }

    @Test
    public void getAncestorsOfNodeTest() {
        Assertions.assertThrows(InvalidNodeRefException.class,
()->{
            eu.xenit.alfred.api.alfresco.metadata.NodeService alfredApiNodeService = new eu.xenit.alfred.api.alfresco.metadata.NodeService(
                    serviceRegistry, alfredApiAlfrescoConverter);
            NodeRef rootRef = new NodeRef(testNode3.toString());
            NodeRef testNode = new NodeRef(testNode1.toString());

            alfredApiNodeService.getAncestors(testNode, rootRef);
        }, "Expected an InvalidNodeRefException to be thrown");
    }
}
