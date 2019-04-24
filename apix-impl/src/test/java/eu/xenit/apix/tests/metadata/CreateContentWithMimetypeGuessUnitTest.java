package eu.xenit.apix.tests.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Locale;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateContentWithMimetypeGuessUnitTest {

    private static final Logger log = LoggerFactory.getLogger(CreateContentWithMimetypeGuessUnitTest.class);

    @Test
    public void createContentWithMimetypeGuess() {
        String fileName = "test";
        String mimeType = "text/plain";
        String contentStr = "TEST CONTENT";
        String encoding = "UTF-8";
        InputStream inputStream = null;

        ServiceRegistry serviceRegistryMock = mock(ServiceRegistry.class);

        MimetypeService mimeTypeServiceMock = mock(MimetypeService.class);
        when(serviceRegistryMock.getMimetypeService()).thenReturn(mimeTypeServiceMock);

        ContentService contentServiceMock = mock(ContentService.class);
        when(serviceRegistryMock.getContentService()).thenReturn(contentServiceMock);

        ContentWriter writer = new StubContentWriter();
        writer = spy(writer);
        when(contentServiceMock.getWriter(null, ContentModel.PROP_CONTENT, false)).thenReturn(writer);

        INodeService nodeService = new NodeService(serviceRegistryMock, new ApixToAlfrescoConversion(serviceRegistryMock));

        try {
            inputStream = new ByteArrayInputStream(contentStr.getBytes(encoding));
            when(mimeTypeServiceMock.guessMimetype(eq(fileName), any(InputStream.class))).thenReturn(mimeType);

            ContentData actualContentData = nodeService.createContentWithMimetypeGuess(inputStream, fileName, encoding);
            verify(mimeTypeServiceMock).guessMimetype(eq(fileName), any(InputStream.class));
            verify(contentServiceMock).getWriter(isNull(NodeRef.class), eq(ContentModel.PROP_CONTENT), anyBoolean());
            verify(writer).setMimetype(eq(mimeType));
            verify(writer).setEncoding(eq(encoding));
            verify(writer).putContent(any(InputStream.class));
            verify(mimeTypeServiceMock).guessMimetype(eq(fileName), any(InputStream.class));
            assertEquals(mimeType, actualContentData.getMimetype());
            assertEquals(encoding, actualContentData.getEncoding());
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            log.error("An unsupportedEncodingException was caught", unsupportedEncodingException);
            fail();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    class StubContentWriter implements ContentWriter {

        public String mimetype;
        public String encoding;

        public StubContentWriter() {
        }

        @Override
        public boolean isChannelOpen() {
            return false;
        }

        @Override
        public void addListener(ContentStreamListener listener) {

        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public org.alfresco.service.cmr.repository.ContentData getContentData() {
            return null;
        }

        @Override
        public String getContentUrl() {
            return null;
        }

        @Override
        public String getMimetype() {
            return this.mimetype;
        }

        @Override
        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }

        @Override
        public String getEncoding() {
            return this.encoding;
        }

        @Override
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public void setLocale(Locale locale) {

        }

        @Override
        public ContentReader getReader() throws ContentIOException {
            return null;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public WritableByteChannel getWritableChannel() throws ContentIOException {
            return null;
        }

        @Override
        public FileChannel getFileChannel(boolean truncate) throws ContentIOException {
            return null;
        }

        @Override
        public OutputStream getContentOutputStream() throws ContentIOException {
            return null;
        }

        @Override
        public void putContent(ContentReader reader) throws ContentIOException {

        }

        @Override
        public void putContent(InputStream iStream) {
            System.out.println("Putting content");
        }

        @Override
        public void putContent(File file) throws ContentIOException {

        }

        @Override
        public void putContent(String content) throws ContentIOException {

        }

        @Override
        public void guessMimetype(String filename) {

        }

        @Override
        public void guessEncoding() {

        }
    }

}
