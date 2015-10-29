package eu.xenit.apix.translation;

import eu.xenit.apix.data.QName;

import java.util.Map;

/**
 * Datastructure that represents a value of a translation. qname: The qname of the property title: The title (which is
 * translated) description: A short translated description.
 */
public class TranslationValue {

    private QName qname;
    private String title;
    private String description;

    public TranslationValue() {
    }

    public TranslationValue(QName qname, String title, String description) {
        this.qname = qname;
        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return "TranslationValue{" +
                "qname=" + qname +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranslationValue that = (TranslationValue) o;

        if (qname != null ? !qname.equals(that.qname) : that.qname != null) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        return description != null ? description.equals(that.description) : that.description == null;

    }

    @Override
    public int hashCode() {
        int result = qname != null ? qname.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
