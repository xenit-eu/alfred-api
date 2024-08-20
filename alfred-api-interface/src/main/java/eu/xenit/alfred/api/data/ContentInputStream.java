package eu.xenit.alfred.api.data;

import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Giovanni on 06/09/16.
 */
public class ContentInputStream {

    private final InputStream inputStream;
    private final String mimetype;
    private final long size;
    private final String encoding;
    private final Locale locale;


    public ContentInputStream(InputStream inputStream, String mimetype, long size, String encoding, Locale locale) {
        this.inputStream = inputStream;
        this.mimetype = mimetype;
        this.size = size;
        this.encoding = encoding;
        this.locale = locale;
    }

    /**
     * @return The Java.io InputStream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @return The mimetype of the content.
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @return The size of the content.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return The encoding of the content.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return The locale of the content.
     */
    public Locale getLocale() {
        return locale;
    }

}
