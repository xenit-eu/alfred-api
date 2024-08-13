package eu.xenit.alfred.api.search.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a range of values on which can be searched.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
//@JsonDeserialize(using=BeanDeserializer.class)
public class RangeValue {

    private String start;
    private String end;

    public RangeValue(@JsonProperty("start") String start, @JsonProperty("end") String end) {
        this.start = start;
        this.end = end;
    }

    /*@Override
    public <T> T accept(ISearchSyntaxVisitor<T> visitor) {
        return visitor.visit(this);
    }*/

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
