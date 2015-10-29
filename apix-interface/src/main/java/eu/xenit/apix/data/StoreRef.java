package eu.xenit.apix.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents a store id. This consists of two parts: 1) Store Protocol - that is, the type of store 2) Store Identifier
 * - the id of the store
 */
public class StoreRef {

    private static final String SPACESTORE_DIVIDER = "://";

    private String value;

    public StoreRef() {}

    @JsonCreator
    public StoreRef(String s) {
        value = s;
    }

    public StoreRef(String space, String store) {
        value = String.format("%s://%s", space, store);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StoreRef nodeRef = (StoreRef) o;

        return value != null ? value.equals(nodeRef.value) : nodeRef.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public String getProtocol() {
        int dividerPatternPosition = value.indexOf(SPACESTORE_DIVIDER);
        return value.substring(0, dividerPatternPosition);
    }

    public String getId() {
        int dividerPatternPosition = value.indexOf(SPACESTORE_DIVIDER);
        return value.substring(dividerPatternPosition + 3);
    }
}
