package eu.xenit.swagger.reader;

import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.SimpleType;
import eu.xenit.swagger.reader.ext.AbstractSwaggerExtension;
import eu.xenit.swagger.reader.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Michiel Huygen on 10/03/2016.
 */
public class DESwaggerExtension extends AbstractSwaggerExtension {

    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip,
            Iterator<SwaggerExtension> chain) {
        Class<?> cls = null;
        if (type instanceof SimpleType) {
            cls = ((SimpleType) type).getRawClass(); // No clue why this happens
        } else if (type instanceof Class) {
            cls = ((Class) type);
        } else if (type instanceof ArrayType) {
            cls = ((ArrayType) type).getRawClass();
        }

        if (cls.isAssignableFrom(WebScriptRequest.class)
                || cls.isAssignableFrom(WebScriptResponse.class)) {
            typesToSkip.add(type);
            return Collections.emptyList();

        }

        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(RequestParam.class)) {
                RequestParam c = (RequestParam) ann;

                QueryParameter parameter = new QueryParameter();
                parameter.setRequired(c.required());
                parameter.setName(c.value());
                parameter.setType("string"); //TODO

                return Collections.singletonList((Parameter) parameter);

            }
        }

        return Collections.emptyList();
        // SHould i call parent?
        // return super.extractParameters(annotations, type, typesToSkip, chain);
    }
}
