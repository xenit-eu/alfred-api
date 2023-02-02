package eu.xenit.apix.rest.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serializable;

public abstract class Jackson2ApixAbstractSerializer<T extends Serializable> extends StdSerializer<T> {

    private static final long serialVersionUID = 1L;
    private final Class<T> supportedClass;

    protected Jackson2ApixAbstractSerializer(Class<T> clazz) {
        super(clazz);
        this.supportedClass = clazz;
    }

    @Override
    public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }

    @Override
    public Class<T> handledType() {
        return supportedClass;
    }
}
