package eu.xenit.apix.alfresco.metadata;


import java.io.IOException;
import java.io.InputStream;
import org.alfresco.service.cmr.repository.ContentReader;

public class DelayedInputStream extends InputStream {

    public InputStream delayed;
    private ContentReader contentReader;

    public DelayedInputStream(ContentReader contentReader) {
        this.contentReader = contentReader;
    }

    @Override
    public int read() throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.read();
    }

    @Override
    public int read(byte[] var1) throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.read(var1);
    }

    @Override
    public int read(byte[] var1, int var2, int var3) throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.read(var1, var2, var3);
    }

    @Override
    public long skip(long var1) throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.skip(var1);
    }

    @Override
    public int available() throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.available();
    }

    @Override
    public void close() throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        delayed.close();
    }

    @Override
    public synchronized void mark(int var1) {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        delayed.mark(var1);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        delayed.reset();
    }

    @Override
    public boolean markSupported() {
        if (delayed == null) {
            delayed = contentReader.getContentInputStream();
        }
        return delayed.markSupported();
    }
}
