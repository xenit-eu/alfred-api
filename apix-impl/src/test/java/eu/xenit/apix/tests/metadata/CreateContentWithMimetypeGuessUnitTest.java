package eu.xenit.apix.tests.metadata;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.node.INodeService;

import java.io.*;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateContentWithMimetypeGuessUnitTest {

    private static final Logger log = LoggerFactory.getLogger(CreateContentWithMimetypeGuessUnitTest.class);

    @Ignore
    public void createContentWithMimetypeGuess() throws IOException {
        String mimeType = "text/plain";
        String encoding = "UTF-8";

        File testFile = new File("test.txt");
        testFile.createNewFile();
        testFile.deleteOnExit();
        InputStream inputStream = null;

        // Set up mocks
        MimetypeService mimeTypeServiceMock = mock(MimetypeService.class);
        ContentService contentServiceMock = mock(ContentService.class);

        ContentWriter writer = mock(ContentWriter.class, withSettings().extraInterfaces(MimetypeServiceAware.class));
        when(contentServiceMock.getWriter(null, ContentModel.PROP_CONTENT, false)).thenReturn(writer);
        when(mimeTypeServiceMock.guessMimetype(/*eq(fileName)*/anyString(), any(ContentReader.class))).thenReturn(mimeType);

        ServiceRegistry serviceRegistryMock = mock(ServiceRegistry.class);
        when(serviceRegistryMock.getMimetypeService()).thenReturn(mimeTypeServiceMock);
        when(serviceRegistryMock.getContentService()).thenReturn(contentServiceMock);
        INodeService nodeService = new NodeService(serviceRegistryMock, new ApixToAlfrescoConversion(serviceRegistryMock));

        try {
            inputStream = new FileInputStream(testFile);

            ContentData contentData = nodeService.createContentWithMimetypeGuess(inputStream, testFile.getName(), encoding);

            verify(writer).guessMimetype(eq(testFile.getName()));
            verify(writer).putContent(any(InputStream.class));
            verify(writer).setEncoding(encoding);
            // The problem is that the writer mock has no notion of a Mimetypeservice
            // (getMimetypeService function is missing)
            verify(writer).isClosed();

            verify(writer).setMimetype(mimeType);
            verify(mimeTypeServiceMock).guessMimetype(/*eq(fileName)*/ anyString(), any(ContentReader.class));

            assertEquals(mimeType, contentData.getMimetype());
            assertEquals(encoding, contentData.getEncoding());
        } finally {
            IOUtils.closeQuietly(inputStream);
            testFile.delete();
        }
    }
}