package eu.xenit.apix.webscripts.resolutions;

import eu.xenit.apix.webscripts.AnnotationWebScriptRequest;
import eu.xenit.apix.webscripts.AnnotationWebscriptResponse;
import javax.annotation.Nonnull;

/**
 * Basic contract for an operation that can generate output based on request data or implementation state or behaviour.
 * <br/> This is the preferred return type for @Uri methods as it allows for reusable response handling.
 *
 * @author Laurent Van der Linden
 */
public interface Resolution {

    void resolve(@Nonnull final AnnotationWebScriptRequest request,
            @Nonnull final AnnotationWebscriptResponse response,
            @Nonnull final ResolutionParameters params) throws Exception;
}
