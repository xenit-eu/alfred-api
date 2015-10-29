package eu.xenit.apix.tests.helperClasses.alfresco.entities;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Locale;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentWriterStub implements ContentWriter {

    private static final Logger log = LoggerFactory.getLogger(ContentWriterStub.class);

    private String contentUrl;
    private String mimeType;
    private String encoding;
    private long size;
    private Locale locale;

    public ContentWriterStub() {
        this.contentUrl="contentUrl Stub";
        this.mimeType="mimetype stub";
        this.encoding="encoding stub";
        this.size = 420L;
        this.locale = new Locale("be");
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
    public void putContent(InputStream is) throws ContentIOException {
        try {
            log.info("Putting content from inputstream. Closing stream afterwards");
        } finally {
            IOUtils.closeQuietly(is);
        }
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

    @Override
    public boolean isChannelOpen() {
        return false;
    }

    @Override
    public void addListener(ContentStreamListener listener) {

    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public ContentData getContentData() {
        return null;
    }

    @Override
    public String getContentUrl() {
        return this.contentUrl;
    }

    @Override
    public String getMimetype() {
        return this.mimeType;
    }

    @Override
    public void setMimetype(String mimetype) {
        this.mimeType = mimetype;
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
        return this.locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
