package eu.xenit.alfred.api.rest.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.search.json.SearchNodeJsonParser;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class ObjectMapperFactory {
    public static ObjectMapper getNewObjectMapper(RestJsonModule alfrescoRestJsonModule) {
        ObjectMapper om = new SearchNodeJsonParser().getObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2ObjectMapperBuilder(alfrescoRestJsonModule).configure(om);
        return om;
    }

    private static Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(RestJsonModule alfrescoRestJsonModule) {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json().failOnEmptyBeans(false)
                .failOnUnknownProperties(false).dateFormat(dateFormat())
                .serializers(customJsonSerilizers().toArray(new JsonSerializer[0]))
                .deserializers(customJsonDeserializers().toArray(new JsonDeserializer[0]))
                .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .findModulesViaServiceLoader(true);

        if (alfrescoRestJsonModule != null) {
            builder.modulesToInstall(alfrescoRestJsonModule);
        }

        return builder;
    }

    private static DateFormat dateFormat() {
        DateFormat dateFormatIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormatIso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormatIso8601;
    }

    private static List<JsonDeserializer<?>> customJsonDeserializers() {
        return Arrays.asList(
                new Jackson2AlfredApiNodeRefDeserializer(),
                new Jackson2AlfredApiQnameDeserializer()
        );
    }

    private static List<JsonSerializer<?>> customJsonSerilizers() {
        return Arrays.asList(
                new Jackson2AlfredApiNodeRefSerializer(),
                new Jackson2AlfredApiQnameSerializer()
        );
    }
}
