package eu.xenit.apix.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data structure that represents part of the search results, specifically the highlights of a single field.
 * A query should return one of these for each field that they enabled highlighting on.
 * Each field may have multiple resulting highlighted snippets.
 */
public class Highlights {
    private Map<String, List<HighlightResult>> noderefs;

    // Empty constructor Needed for Jackson deserialization
    public Highlights() {}

    public Highlights(Map<String, List<HighlightResult>> solrHighlights) {
        setNoderefs(solrHighlights);
    }

    public Map<String, List<HighlightResult>> getNoderefs() {
        return noderefs;
    }
    public void setNoderefs(Map<String, List<HighlightResult>> noderefs) { this.noderefs = noderefs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Highlights)) {
            return false;
        }
        Highlights that = (Highlights) o;
        return Objects.equals(getNoderefs(), that.getNoderefs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNoderefs());
    }

    @Override
    public String toString() {
        return "Highlights{" +
                "noderefs=" + noderefs +
                '}';
    }

    public static class HighlightResult{

        private String field;
        private List<String> snippets;

        public HighlightResult() {
        }

        @JsonCreator
        public HighlightResult(@JsonProperty("field") String field, @JsonProperty("snippets") List<String> snippets) {
            this.field = field;
            this.snippets = snippets;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public List<String> getSnippets() {
            return snippets;
        }

        public void setSnippets(List<String> snippets) {
            this.snippets = snippets;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HighlightResult)) {
                return false;
            }
            HighlightResult that = (HighlightResult) o;
            return getField().equals(that.getField()) &&
                    getSnippets().equals(that.getSnippets());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getField(), getSnippets());
        }

        @Override
        public String toString() {
            return "HighlightResult{" +
                    "field='" + field + '\'' +
                    ", snippets=" + snippets +
                    '}';
        }
    }
}
