package eu.xenit.apix.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Represents an unique name in alfresco for properties and types.
 */
public class QName implements Serializable {

    private String value;

    @JsonCreator
    public QName() {

    }

    @JsonCreator
    public QName(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        value = s;
    }

    /**
     * @return The qname.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QName qName = (QName) o;

        return value != null ? value.equals(qName.value) : qName.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
