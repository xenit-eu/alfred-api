package eu.xenit.apix.rest.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.xenit.apix.data.QName;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

public class Jackson2ApixQnameDeserializer extends JsonDeserializer<QName> implements Converter<String, QName> {

    @Override
    public Class<?> handledType() {
        return QName.class;
    }

    @Override
    public QName deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return new QName(
            jp.getText()
        );
    }

    @Override
    public QName convert(String qname) {
        return new QName(qname);
    }
}