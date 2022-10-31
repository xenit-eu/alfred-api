package eu.xenit.apix.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.rest.v1.nodes.NodesWebscript1;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

public class NodesWebscript1Test {

    @Test
    public void test_uploadNode_triggerMetadataExtract() throws IOException {
        //Setup mocks
        INodeService nodeServiceMock = mock(NodeService.class);
        when(nodeServiceMock.createNode(any(), any(), any()))
                .thenReturn(new NodeRef("workspace://SpacesStore/12345678-1234-1234-1234-123456789012"));
        doNothing().when(nodeServiceMock).setContent(any(), any(), any());
        doNothing().when(nodeServiceMock).extractMetadata(any());

        NodesWebscript1 nodesWebscript = new NodesWebscript1(nodeServiceMock,
                null, null, null, null);

        MultipartFile fileMock = mock(MultipartFile.class);
        when(fileMock.getOriginalFilename()).thenReturn("testFile");
        InputStream inputStreamMock = mock(InputStream.class);
        when(fileMock.getInputStream()).thenReturn(inputStreamMock);

        //Do Test
        nodesWebscript.createNodeForUpload("workspace://SpacesStore/87654321-4321-4321-4321-210987654321",
                fileMock, "cm:content", null, true);
        verify(nodeServiceMock, times(1)).extractMetadata(any());
    }
}
