package eu.xenit.apix.rest.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.node.MetadataChanges;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

public class Jackson2ApixMetadataChangesDeserializer extends JsonDeserializer<MetadataChanges>
                                                    implements Converter<String, MetadataChanges> {

    private final ObjectMapper mapper;

    public Jackson2ApixMetadataChangesDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Class<?> handledType() {
        return MetadataChanges.class;
    }

    @Override
    public MetadataChanges deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return convert(jp.getText());
    }

    @Override
    public MetadataChanges convert(String metadataChanges) {
        try {
            return mapper.readValue(metadataChanges, MetadataChanges.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
