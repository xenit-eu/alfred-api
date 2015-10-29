package eu.xenit.apix.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.data.StoreRef;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a search query with search options.
 * query: The query itself without any options.
 * paging: Options related to paging.
 * facets: options related to facets.
 * orderBy: List which represent in which order the results should be returned.
 * consistency: Options related to consistency. Is defaulted by eventual consistency.
 * highlight: Options related to term hit highlighting, similar to the Alfresco 5.2.4 API
 */
public class SearchQuery {

    @ApiModelProperty(required = true)
    private SearchSyntaxNode query;
    private PagingOptions paging = new PagingOptions();
    private FacetOptions facets = new FacetOptions();
    private List<OrderBy> orderBy = new ArrayList<>();
    private SearchQueryConsistency consistency = SearchQueryConsistency.EVENTUAL;
    private Locale locale = null;
    private StoreRef workspace = null;
    private HighlightOptions highlight = new HighlightOptions();

    public SearchSyntaxNode getQuery() {
        return query;
    }

    public void setQuery(SearchSyntaxNode query) {
        this.query = query;
    }

    public SearchQueryConsistency getConsistency() {
        return consistency;
    }

    public void setConsistency(SearchQueryConsistency consistency) {
        this.consistency = consistency;
    }

    public FacetOptions getFacets() {
        return facets;
    }

    public void setFacets(FacetOptions facets) {
        this.facets = facets;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public PagingOptions getPaging() {
        return paging;
    }

    public void setPaging(PagingOptions paging) {
        this.paging = paging;
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    public HighlightOptions getHighlight() {
        return highlight;
    }

    public void setHighlight(HighlightOptions highlights) {
        this.highlight = highlights;
    }

    public StoreRef getWorkspace() { return workspace; }

    public void setWorkspace(StoreRef workspace) { this.workspace = workspace; }

    public static class PagingOptions {

        private Integer limit = null;
        private int skip = -1;

        public PagingOptions(Integer limit, int skip) {
            this.limit = limit;
            this.skip = skip;
        }

        public PagingOptions() {
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public int getSkip() {
            return skip;
        }

        public void setSkip(int skip) {
            this.skip = skip;
        }
    }

    public static class FacetOptions {

        private boolean enabled;
        @ApiModelProperty("Limits the number of values returned per facet")
        private Integer limit = -1;
        @ApiModelProperty("Return only facet values with count >= mincount")
        private Integer mincount;
        public List<String> custom;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getMincount() {
            return mincount;
        }

        public void setMincount(Integer mincount) {
            this.mincount = mincount;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getCustom() {
            return custom;
        }

        public void setCustom(List<String> custom) {
            this.custom = custom;
        }
    }

    public static class OrderBy {

        private QName property;

        private Order order;

        public OrderBy() {
        }

        public OrderBy(Order order, QName property) {
            this.order = order;
            this.property = property;
        }

        public QName getProperty() {
            return property;
        }

        public void setProperty(QName property) {
            this.property = property;
        }

        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public enum Order {
            DESCENDING("descending"),
            ASCENDING("ascending");

            private final String value;

            Order(String value) {
                this.value = value;
            }

            @JsonCreator
            public static Order forValue(String value) {
                String uppercase = value.toUpperCase();
                return Order.valueOf(uppercase);
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }
    }

    // Basically identical to https://docs.alfresco.com/5.2/concepts/search-api-highlight.html
    public static class HighlightOptions {
        private String prefix;
        private String postfix;
        private Integer snippetCount;
        private Integer fragmentSize;
        private Integer maxAnalyzedCharacters;
        private Boolean mergeContiguous;
        private Boolean usePhraseHighlighter;
        private List<HighlightFieldOption> fields;

        public HighlightOptions() {
            fields = new ArrayList<>();
            fields.add(new HighlightFieldOption());
        }

        public HighlightOptions(String prefix, String postfix, Integer snippetCount, Integer fragmentSize,
                Integer maxAnalyzedCharacters, Boolean mergeContiguous, Boolean usePhraseHighlighter,
                List<HighlightFieldOption> fields) {
            this.prefix = prefix;
            this.postfix = postfix;
            this.snippetCount = snippetCount;
            this.fragmentSize = fragmentSize;
            this.maxAnalyzedCharacters = maxAnalyzedCharacters;
            this.mergeContiguous = mergeContiguous;
            this.usePhraseHighlighter = usePhraseHighlighter;
            this.fields = fields != null ? fields : Arrays.asList(new HighlightFieldOption());
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPostfix() {
            return postfix;
        }

        public void setPostfix(String postfix) {
            this.postfix = postfix;
        }

        public void setSuffix(String suffix) {
            this.postfix = suffix;
        }

        public Integer getSnippetCount() {
            return snippetCount;
        }

        public void setSnippetCount(Integer snippetCount) {
            this.snippetCount = snippetCount;
        }

        public Integer getFragmentSize() {
            return fragmentSize;
        }

        public void setFragmentSize(Integer fragmentSize) {
            this.fragmentSize = fragmentSize;
        }

        public Integer getMaxAnalyzedCharacters() {
            return maxAnalyzedCharacters;
        }

        public void setMaxAnalyzedCharacters(Integer maxAnalyzedCharacters) {
            this.maxAnalyzedCharacters = maxAnalyzedCharacters;
        }

        public Boolean getMergeContiguous() {
            return mergeContiguous;
        }

        public void setMergeContiguous(Boolean mergeContiguous) {
            this.mergeContiguous = mergeContiguous;
        }

        public Boolean getUsePhraseHighlighter() {
            return usePhraseHighlighter;
        }

        public void setUsePhraseHighlighter(Boolean usePhraseHighlighter) {
            this.usePhraseHighlighter = usePhraseHighlighter;
        }

        public List<HighlightFieldOption> getFields() {
            return fields;
        }

        public void setFields(List<HighlightFieldOption> fields) {
            this.fields = fields;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            HighlightOptions that = (HighlightOptions) o;

            if (!Objects.equals(prefix, that.prefix)) {
                return false;
            }
            if (!Objects.equals(postfix, that.postfix)) {
                return false;
            }
            if (!Objects.equals(snippetCount, that.snippetCount)) {
                return false;
            }
            if (!Objects.equals(fragmentSize, that.fragmentSize)) {
                return false;
            }
            if (!Objects.equals(maxAnalyzedCharacters, that.maxAnalyzedCharacters)) {
                return false;
            }
            if (!Objects.equals(mergeContiguous, that.mergeContiguous)) {
                return false;
            }
            if (!Objects.equals(usePhraseHighlighter, that.usePhraseHighlighter)) {
                return false;
            }
            return fields.equals(that.fields);
        }

        public static class HighlightFieldOption {
            public HighlightFieldOption() {
                this.field = "cm:content";
            }

            public HighlightFieldOption(String field, Integer snippetCount, Integer fragmentSize,
                    Boolean mergeContinuous, String prefix, String suffix) {
                this.field = field;
                this.snippetCount = snippetCount;
                this.fragmentSize = fragmentSize;
                this.mergeContinuous = mergeContinuous;
                this.prefix = prefix;
                this.suffix = suffix;
            }

            public String field;
            public Integer snippetCount;
            public Integer fragmentSize;
            public Boolean mergeContinuous;
            public String prefix;
            public String suffix;

            public String getField() {
                return field;
            }

            public void setField(String field) {
                this.field = field;
            }

            public Integer getSnippetCount() {
                return snippetCount;
            }

            public void setSnippetCount(Integer snippetCount) {
                this.snippetCount = snippetCount;
            }

            public Integer getFragmentSize() {
                return fragmentSize;
            }

            public void setFragmentSize(Integer fragmentSize) {
                this.fragmentSize = fragmentSize;
            }

            public Boolean getMergeContinuous() {
                return mergeContinuous;
            }

            public void setMergeContinuous(Boolean mergeContinuous) {
                this.mergeContinuous = mergeContinuous;
            }

            public String getPrefix() {
                return prefix;
            }

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }

            public String getSuffix() {
                return suffix;
            }

            public void setSuffix(String suffix) {
                this.suffix = suffix;
            }
        }
    }
}
