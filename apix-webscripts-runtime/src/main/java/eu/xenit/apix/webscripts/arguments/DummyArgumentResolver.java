package eu.xenit.apix.webscripts.arguments;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import com.github.dynamicextensionsalfresco.webscripts.arguments.ArgumentResolver;
import java.lang.annotation.Annotation;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;


/**
 * I have this class here to trick spring in being able to inject a ArgumentResolver[], since that needs at least one
 * ArgumentResolver in the spring beans space. Created by Michiel Huygen on 06/04/2016.
 */
@Component("eu.xenit.apix.webscripts.arguments.DummyArgumentResolver")
@OsgiService
public class DummyArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supports(Class argumentType, Class annotationType) {
        return false;
    }

    @Override
    public Object resolveArgument(Class argumentType, Annotation parameterAnnotation, String name,
            WebScriptRequest request, WebScriptResponse response) {
        return null;
    }
}
