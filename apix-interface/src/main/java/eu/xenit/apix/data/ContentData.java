package eu.xenit.apix.data;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents the data of a file.
 */
public class ContentData implements Serializable {

    private static final long serialVersionUID = 8979634213050121462L;


    private final String contentUrl;
    private final String mimetype;
    private final long size;
    private final String encoding;
    private final Locale locale;

    public ContentData(String contentUrl, String mimetype, long size, String encoding, Locale locale) {
        this.contentUrl = contentUrl;
        this.mimetype = mimetype;
        this.size = size;
        this.encoding = encoding;
        this.locale = locale;

    }

    /**
     * @return The url that represents the content.
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * @return The mimetype of the content.
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @return The size of the data, expressed in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return The encoding of the content data.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return The locale of the content data.
     */
    public Locale getLocale() {
        return locale;
    }

    public int hashCode() {
        return this.contentUrl != null ? this.contentUrl.hashCode() : 0;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof ContentData that)) {
            return false;
        } else {
            return (Objects.equals(this.contentUrl, that.contentUrl))
                    && (Objects.equals(this.mimetype, that.mimetype))
                    && (this.size == that.size)
                    && (Objects.equals(this.encoding, that.encoding))
                    && (Objects.equals(this.locale, that.locale));
        }
    }

}
