package eu.xenit.apix.rest.v1.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScriptRequest;
import com.github.dynamicextensionsalfresco.webscripts.arguments.ArgumentResolver;
import java.io.IOException;
import java.lang.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 14/03/2016.
 */
@Component
public class ApixArgumentResolver implements ArgumentResolver<Object, Annotation> {

    private Logger logger = LoggerFactory.getLogger(ApixArgumentResolver.class);

    @Override
    public final boolean supports(final Class<?> parameterType, final Class<? extends Annotation> annotationType) {
        /* Determine if using Class.isAssignableFrom() breaks backwards compatibility. */
        return parameterType.getCanonicalName().startsWith("eu.xenit.apix") && !Exception.class.isAssignableFrom(parameterType);
    }

    @Override
    public final Object resolveArgument(final Class<?> argumentType, final Annotation parameterAnnotation,
            final String name, final WebScriptRequest request, final WebScriptResponse response) {
        /*final Class<?> expectedParameterType = getExpectedArgumentType();
        if (argumentType.equals(expectedParameterType) == false) {
            throw new IllegalArgumentException(String.format("Incorrect parameter type %s, expected type %s",
                    argumentType, expectedParameterType));
        }*/

        try {
            //TODO: inject apix object mapper?
            ObjectMapper map = new ObjectMapper();
            return map.readValue(request.getContent().getContent(), argumentType);
        } catch (IOException e) {
            logger.warn("Cannot convert webscript argument with type from package eu.xenit.apix to json", e);
            throw new RuntimeException("Cannot convert webscript argument (" + name + ") " +
                    "with type from package eu.xenit.apix to json - " + argumentType.getCanonicalName(), e);
        }
    }

}
