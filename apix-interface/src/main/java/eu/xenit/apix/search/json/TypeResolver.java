package eu.xenit.apix.search.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import java.util.Collection;

public class TypeResolver implements TypeResolverBuilder<TypeResolver> {

    @Override
    public Class<?> getDefaultImpl() {
        return null;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType,
            Collection<NamedType> subtypes) {
        return null;
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType,
            Collection<NamedType> subtypes) {
        TypeNameIdResolver idRes = TypeNameIdResolver.construct(config, baseType, subtypes, false, true);
        return new SearchNodeTypeDeserializer(baseType, idRes, null, false, null);
    }

    @Override
    public TypeResolver init(JsonTypeInfo.Id idType, TypeIdResolver res) {
        return this;
    }

    @Override
    public TypeResolver inclusion(JsonTypeInfo.As includeAs) {
        return this;
    }

    @Override
    public TypeResolver typeProperty(String propName) {
        return this;
    }

    @Override
    public TypeResolver defaultImpl(Class<?> defaultImpl) {
        return this;
    }

    @Override
    public TypeResolver typeIdVisibility(boolean isVisible) {
        return this;
    }
}
