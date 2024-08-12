package eu.xenit.apix.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identifier a single node. Consists of three parts: The space, the store and the guid. This can be seen as pointing to
 * the Store and then a unique id within the store for the node.
 */
public class NodeRef implements Serializable {

    private static final long serialVersionUID = 3979634213023421462L;

    private static final String FORWARD_SLASH = "/";
    private static final String SPACESTORE_DIVIDER = "://";
    private String value;

    public NodeRef() {
    }

    @JsonCreator
    public NodeRef(String s) {
        value = s;
    }

    public NodeRef(String space, String store, String guid) {
        value = String.format("%s://%s/%s", space, store, guid);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String GetApixUrl() {
        return getStoreRefProtocol() + "/" + getStoreRefId() + "/" + getGuid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeRef nodeRef = (NodeRef) o;

        return Objects.equals(value, nodeRef.value);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * @return The space or storerefprotocol of the noderef. This is the first part of the noderef.
     */
    public String getStoreRefProtocol() {
        int lastForwardSlash = value.lastIndexOf(FORWARD_SLASH);
        String storeRef = value.substring(0, lastForwardSlash);
        int dividerPatternPosition = storeRef.indexOf(SPACESTORE_DIVIDER);
        return storeRef.substring(0, dividerPatternPosition);
    }

    /**
     * @return The store of the noderef. This is the second part of the noderef.
     */
    public String getStoreRefId() {
        int lastForwardSlash = value.lastIndexOf(FORWARD_SLASH);
        String storeRef = value.substring(0, lastForwardSlash);
        int dividerPatternPosition = storeRef.indexOf(SPACESTORE_DIVIDER);
        return storeRef.substring(dividerPatternPosition + 3);
    }

    /**
     * @return The Guid part of the noderef. This is the last part of the noderef.
     */
    public String getGuid() {
        int lastForwardSlash = value.lastIndexOf(FORWARD_SLASH);
        return value.substring(lastForwardSlash + 1);
    }
}
