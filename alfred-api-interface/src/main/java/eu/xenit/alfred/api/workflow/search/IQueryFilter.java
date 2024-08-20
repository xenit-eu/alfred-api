package eu.xenit.alfred.api.workflow.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PropertyFilter.class, name = PropertyFilter.TYPE),
        @JsonSubTypes.Type(value = DateRangeFilter.class, name = DateRangeFilter.TYPE),
})
public interface IQueryFilter {

    String getType();

    String getProperty();
}
