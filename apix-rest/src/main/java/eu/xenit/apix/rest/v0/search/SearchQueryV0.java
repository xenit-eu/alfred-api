package eu.xenit.apix.rest.v0.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michiel Huygen on 05/11/2015.
 */
public class SearchQueryV0 {

    private SearchSyntaxNode query;
    private Integer limit = null;
    private int skip = -1;
    private FacetOptions facets = new FacetOptions();
    private List<OrderBy> orderBy = new ArrayList<>();

    public SearchSyntaxNode getQuery() {
        return query;
    }

    public void setQuery(SearchSyntaxNode query) {
        this.query = query;
    }


    public FacetOptions getFacets() {
        return facets;
    }

    public void setFacets(FacetOptions facets) {
        this.facets = facets;
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

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    public SearchQuery toV1() {
        SearchQuery ret = new SearchQuery();
        ret.setQuery(getQuery());
        ret.setPaging(new SearchQuery.PagingOptions(getLimit(), getSkip()));
        // Facets
        ret.setFacets(new SearchQuery.FacetOptions());
        ret.getFacets().setLimit(getFacets().getLimit());
        ret.getFacets().setMincount(getFacets().getMincount());
        ret.getFacets().setEnabled(true);

        // Orderby
        ret.setOrderBy(new ArrayList<SearchQuery.OrderBy>());
        for (OrderBy o : getOrderBy()) {
            ret.getOrderBy().add(new SearchQuery.OrderBy(SearchQuery.OrderBy.Order.forValue(o.getOrder().toString()),
                    o.getProperty()));
        }

        return ret;
    }

    public static class FacetOptions {

        //private ArrayList<FacetFieldOptions> fields;
        private boolean retrieveDefaults = false;
        private Integer limit = -1;
        private Integer mincount;

        public boolean isRetrieveDefaults() {
            return retrieveDefaults;
        }

        public void setRetrieveDefaults(boolean retrieveDefaults) {
            this.retrieveDefaults = retrieveDefaults;
        }

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
    }

    public static class OrderBy {

        private QName property;

        private Order order;

        public QName getProperty() {
            return property;
        }

        //private String expression;

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

//        public String getExpression() {
//            return expression;
//        }
//
//        public void setExpression(String expression) {
//            this.expression = expression;
//        }
    }
}
