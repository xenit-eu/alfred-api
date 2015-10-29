package eu.xenit.apix.webscripts.resolutions;

import java.lang.reflect.Method;
import org.springframework.extensions.webscripts.Description;

/**
 * @author Laurent Van der Linden
 */
public interface ResolutionParameters {

    Method getUriMethod();

    Description getDescription();

    Object getHandler();

    Class<?> getHandlerClass();
}
