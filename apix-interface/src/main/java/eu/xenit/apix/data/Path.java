package eu.xenit.apix.data;

import java.util.Objects;

/**
 * Represents a path to a node. Has a string value.
 */
public class Path {

    private final String value;

    public Path(String value) {
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

        Path path = (Path) o;

        return Objects.equals(value, path.value);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
