package eu.xenit.apix.search;

import eu.xenit.apix.search.nodes.RangeValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data structure that represents a facet of a search result.
 *
 * @param name: The name of the facet
 * @param values: A list with all the possible facet values.
 */
public class FacetSearchResult {

    private String name;
    private List<FacetValue> values = new ArrayList<>();


    public FacetSearchResult() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FacetValue> getValues() {
        return values;
    }

    public void setValues(List<FacetValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "FacetSearchResult{" +
                "name='" + name + '\'' +
                ", values=" + values +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FacetSearchResult)) {
            return false;
        }
        FacetSearchResult that = (FacetSearchResult) o;
        return Objects.equals(getName(), that.getName()) &&
              Objects.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValues());
    }

    public static class FacetValue {

        private String value;
        private RangeValue range;
        private String label;
        private int count;

        public FacetValue() {
        }

        public FacetValue(String value, String label, int count) {
            this.value = value;
            this.label = label;
            this.count = count;
        }

        public FacetValue(RangeValue range, String label, int count) {
            this.range = range;
            this.label = label;
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public RangeValue getRange() {
            return range;
        }

        public void setRange(RangeValue range) {
            this.range = range;
        }

        @Override
        public String toString() {
            return "FacetValue{" +
                    "value='" + value + '\'' +
                    ", range=" + range +
                    ", label='" + label + '\'' +
                    ", count=" + count +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FacetValue)) {
                return false;
            }
            FacetValue that = (FacetValue) o;
            return getCount() == that.getCount() &&
                  Objects.equals(getValue(), that.getValue()) &&
                  Objects.equals(getRange(), that.getRange()) &&
                  Objects.equals(getLabel(), that.getLabel());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue(), getRange(), getLabel(), getCount());
        }
    }
}
