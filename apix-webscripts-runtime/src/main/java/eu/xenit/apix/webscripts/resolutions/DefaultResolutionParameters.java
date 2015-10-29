package eu.xenit.apix.webscripts.resolutions;

import java.lang.reflect.Method;
import org.springframework.aop.support.AopUtils;
import org.springframework.extensions.webscripts.Description;

/**
 * @author Laurent Van der Linden
 */
public class DefaultResolutionParameters implements ResolutionParameters {

    private final Method uriMethod;
    private final Description description;
    private final Object handler;

    public DefaultResolutionParameters(Method uriMethod, Description description, Object handler) {
        this.uriMethod = uriMethod;
        this.description = description;
        this.handler = handler;
    }

    @Override
    public Method getUriMethod() {
        return uriMethod;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public Object getHandler() {
        return handler;
    }

    @Override
    public Class<?> getHandlerClass() {
        return AopUtils.getTargetClass(getHandler());
    }
}
