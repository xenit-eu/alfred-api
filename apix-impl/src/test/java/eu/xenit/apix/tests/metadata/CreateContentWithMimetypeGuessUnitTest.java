package eu.xenit.apix.tests.metadata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateContentWithMimetypeGuessUnitTest {

    private static final Logger log = LoggerFactory.getLogger(CreateContentWithMimetypeGuessUnitTest.class);

    @Test
    public void createContentWithMimetypeGuess() throws UnsupportedEncodingException {
        String fileName = "test";
        String mimeType = "text/plain";
        String encoding = "UTF-8";
        InputStream inputStream = null;

        // Set up mocks
        MimetypeService mimeTypeServiceMock = mock(MimetypeService.class);
        when(mimeTypeServiceMock.guessMimetype(eq(fileName), any(InputStream.class))).thenReturn(mimeType);

        ContentService contentServiceMock = mock(ContentService.class);

        ContentWriter writer = mock(ContentWriter.class);
        when(contentServiceMock.getWriter(null, ContentModel.PROP_CONTENT, false)).thenReturn(writer);

        ServiceRegistry serviceRegistryMock = mock(ServiceRegistry.class);
        when(serviceRegistryMock.getMimetypeService()).thenReturn(mimeTypeServiceMock);
        when(serviceRegistryMock.getContentService()).thenReturn(contentServiceMock);

        INodeService nodeService = new NodeService(serviceRegistryMock, new ApixToAlfrescoConversion(serviceRegistryMock));

        try {
            inputStream = new ByteArrayInputStream("TEST CONTENT".getBytes(encoding));
            ContentData contentData = nodeService.createContentWithMimetypeGuess(inputStream, fileName, encoding);

            verify(mimeTypeServiceMock).guessMimetype(eq(fileName), any(InputStream.class));
            verify(writer).setMimetype(mimeType);
            verify(writer).setEncoding(encoding);
            verify(writer).putContent(any(InputStream.class));
            verify(mimeTypeServiceMock).guessMimetype(eq(fileName), any(InputStream.class));

            assertEquals(mimeType, contentData.getMimetype());
            assertEquals(encoding, contentData.getEncoding());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
