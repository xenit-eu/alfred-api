package eu.xenit.apix.webscripts.resolutions;

import eu.xenit.apix.webscripts.AnnotationWebScriptRequest;
import eu.xenit.apix.webscripts.AnnotationWebscriptResponse;
import java.io.IOException;

/**
 * @author Laurent Van der Linden
 * @deprecated replaced with {@see StatusResolution}
 */
public class ErrorResolution implements Resolution {

    private int status;
    private String message;

    public ErrorResolution(int status) {
        this.status = status;
    }

    public ErrorResolution(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public void resolve(AnnotationWebScriptRequest request, AnnotationWebscriptResponse response,
            ResolutionParameters params) throws IOException {
        response.setStatus(status);
        if (message != null) {
            response.getWriter().append(message);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
