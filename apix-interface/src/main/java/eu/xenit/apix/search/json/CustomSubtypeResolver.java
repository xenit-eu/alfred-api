package eu.xenit.apix.search.json;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import java.util.Collection;
import java.util.HashMap;

/**
 * Resolves subtypes.
 */
class CustomSubtypeResolver extends SubtypeResolver {

    private final HashMap<String, NamedType> registeredTypes;

    public CustomSubtypeResolver() {
        registeredTypes = new HashMap<String, NamedType>();

    }

    /**
     * Registers given types to this resolver.
     *
     * @param types The types to register
     */
    @Override
    public void registerSubtypes(NamedType... types) {
        for (NamedType t : types) {
            if (registeredTypes.containsKey(t.getName())) {
                throw new UnsupportedOperationException("Already configured a type with that name");
            }
            registeredTypes.put(t.getName(), t);
        }
    }

    /**
     * Not Supported at the moment
     */
    @Override
    public void registerSubtypes(Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerSubtypes(Collection<Class<?>> collection) {
    }


    /**
     * Not Supported at the moment
     */
    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedMember property, MapperConfig<?> config,
            AnnotationIntrospector ai, JavaType baseType) {
        throw new UnsupportedOperationException();
        //return null;
    }

    /**
     * Returns all the registered types.
     */
    @Override
    public Collection<NamedType> collectAndResolveSubtypes(AnnotatedClass basetype, MapperConfig<?> config,
            AnnotationIntrospector ai) {
        return registeredTypes.values();
       /* if (!basetype.getRawType().equals(SearchSyntaxNode.class)) return null;

        ArrayList<NamedType> types = new ArrayList<NamedType>();
        types.add(new NamedType(TermSearchNode.class, "type"));
        types.add(new NamedType(TermSearchNode.class, "aspect"));
        return types;*/
    }
}
