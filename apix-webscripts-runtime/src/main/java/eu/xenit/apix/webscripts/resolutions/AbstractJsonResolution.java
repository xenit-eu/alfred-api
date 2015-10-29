package eu.xenit.apix.webscripts.resolutions;

import eu.xenit.apix.webscripts.AnnotationWebScriptRequest;
import eu.xenit.apix.webscripts.AnnotationWebscriptResponse;
import javax.annotation.Nonnull;
import org.alfresco.repo.content.MimetypeMap;

/**
 * @author Laurent Van der Linden
 */
public abstract class AbstractJsonResolution extends AbstractResolution {

    public static final String UTF_8 = "UTF-8";

    private Integer status = null;

    protected AbstractJsonResolution() {
    }

    /**
     * @param status http status code
     * @deprecated use {@link #withStatus(int)} instead
     */
    protected AbstractJsonResolution(int status) {
        this.status = status;
    }

    @Override
    public void resolve(@Nonnull AnnotationWebScriptRequest request,
            @Nonnull AnnotationWebscriptResponse response,
            @Nonnull ResolutionParameters params) throws Exception {
        super.resolve(request, response, params);
        response.setContentType(MimetypeMap.MIMETYPE_JSON);
        response.setContentEncoding(UTF_8);
        response.setHeader("Cache-Control", "no-cache,no-store");

        if (status != null) {
            response.setStatus(status);
        }
    }
}
