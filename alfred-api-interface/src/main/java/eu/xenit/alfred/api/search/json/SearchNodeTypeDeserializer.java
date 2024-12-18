package eu.xenit.alfred.api.search.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import java.io.IOException;

/**
 * Type deserializer. Simple since JSON structure used is always the same, regardless of structure used for actual
 * value: wrapping is done using a single-element JSON Object where type id is the key, and actual object data as the
 * value.
 */
public class SearchNodeTypeDeserializer
        extends TypeDeserializerBase
        implements java.io.Serializable {

    public SearchNodeTypeDeserializer(JavaType bt, TypeIdResolver idRes,
            String typePropertyName, boolean typeIdVisible, Class<?> defaultImpl) {
        super(bt, idRes, typePropertyName, typeIdVisible, null);
    }

    protected SearchNodeTypeDeserializer(SearchNodeTypeDeserializer src, BeanProperty property) {
        super(src, property);
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        if (prop == _property) { // usually if it's null
            return this;
        }
        return new SearchNodeTypeDeserializer(this, prop);
    }

    @Override
    public As getTypeInclusion() {
        return As.WRAPPER_OBJECT;
    }

    /**
     * Deserializing type id enclosed using WRAPPER_OBJECT style is straightforward
     */
    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return _deserialize(jp, ctxt);
    }

    @Override
    public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return _deserialize(jp, ctxt);
    }

    @Override
    public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return _deserialize(jp, ctxt);
    }

    @Override
    public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return _deserialize(jp, ctxt);
    }

    /*
    /***************************************************************
    /* Internal methods
    /***************************************************************
     */

    /**
     * Method that handles type information wrapper, locates actual subtype deserializer to use, and calls it to do
     * actual deserialization.
     */
    @SuppressWarnings("resource")
    private final Object _deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        // 02-Aug-2013, tatu: May need to use native type ids
        if (jp.canReadTypeId()) {
            return _deserializeWithNativeTypeId(jp, ctxt);
        }

        // first, sanity checks
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT,
                    "need JSON Object to contain As.WRAPPER_OBJECT type information for class " + baseTypeName());
        }
        // should always get field name, but just in case...
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME,
                    "need JSON String that contains type id (for subtype of " + baseTypeName() + ")");
        }
        final String typeId = jp.getText();
        JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
        jp.nextToken();

        // Minor complication: we may need to merge type id in?
        /*if (_typeIdVisible && jp.getCurrentToken() == JsonToken.START_OBJECT) {
            // but what if there's nowhere to add it in? Error? Or skip? For now, skip.
            TokenBuffer tb = new TokenBuffer(null, false);
            tb.writeStartObject(); // recreate START_OBJECT
            tb.writeFieldName(_typePropertyName);
            tb.writeString(typeId);
            jp = JsonParserSequence.createFlattened(tb.asParser(jp), jp);
            jp.nextToken();
        }*/

        Object value = deser.deserialize(jp, ctxt);

        if (value instanceof IJsonTyped) {
            ((IJsonTyped) value).setTypeId(typeId);
        }

        // And then need the closing END_OBJECT
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_OBJECT,
                    "expected closing END_OBJECT after type information and deserialized value");
        }
        return value;
    }
}
