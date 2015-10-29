package eu.xenit.apix.data;

import java.io.Serializable;
import java.util.Locale;

/**
 * Represents the data of a file.
 */
public class ContentData implements Serializable {

    private static final long serialVersionUID = 8979634213050121462L;


    private String contentUrl;
    private String mimetype;
    private long size;
    private String encoding;
    private Locale locale;

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
        } else if (!(obj instanceof ContentData)) {
            return false;
        } else {
            ContentData that = (ContentData) obj;
            return (this.contentUrl != null ? this.contentUrl.equals(that.contentUrl) : that.contentUrl == null)
                    && (this.mimetype != null ? this.mimetype.equals(that.mimetype) : that.mimetype == null)
                    && (this.size == that.size)
                    && (this.encoding != null ? this.encoding.equals(that.encoding) : that.encoding == null)
                    && (this.locale != null ? this.locale.equals(that.locale) : that.locale == null);
        }
    }

}
