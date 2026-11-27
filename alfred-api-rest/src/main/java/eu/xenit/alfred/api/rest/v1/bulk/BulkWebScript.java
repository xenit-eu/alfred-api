package eu.xenit.alfred.api.rest.v1.bulk;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.rest.v1.bulk.request.BulkHttpServletRequest;
import eu.xenit.alfred.api.rest.v1.bulk.request.BulkRequest;
import eu.xenit.alfred.api.rest.v1.bulk.response.IntermediateResponse;
import eu.xenit.alfred.api.rest.webscript.JsonRequests;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;
import org.springframework.stereotype.Component;

/**
 * POST /apix/v1/bulk: runs several /apix/v1 requests and returns each one's status, headers and body.
 * <p>
 * Each sub-request is resolved and executed by the web script runtime like a direct request, so it gets the
 * transaction its own web script declares. This web script has no transaction of its own ({@code none} in its
 * descriptor): a sub-request would otherwise join it instead of getting a separate one.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.bulk.bulk.post")
public class BulkWebScript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(BulkWebScript.class);

    private static final ObjectMapper BODY_MAPPER = new ObjectMapper();

    private final RuntimeContainer container;

    public BulkWebScript(@Qualifier("webscripts.container") RuntimeContainer container) {
        this.container = container;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        BulkRequest[] bulkRequests = JsonRequests.readBody(req, BulkRequest[].class);
        HttpServletRequest httpRequest = WebScriptServletRuntime.getHttpServletRequest(req);
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        List<BulkSubResult> results = new ArrayList<>();
        for (BulkRequest bulkRequest : bulkRequests) {
            results.add(execute(httpRequest, bulkRequest, userName));
        }

        if (results.isEmpty()) {
            res.setStatus(Status.STATUS_NO_CONTENT);
            res.setHeader("Cache-Control", "no-cache");
            return;
        }
        JsonResponses.writeJson(res, results);
    }

    private BulkSubResult execute(HttpServletRequest httpRequest, BulkRequest bulkRequest, String userName) {
        String url = bulkRequest.getUrl();
        AuthenticationUtil.pushAuthentication();
        try {
            BulkHttpServletRequest subRequest = new BulkHttpServletRequest(httpRequest, url,
                    bulkRequest.getMethod().toUpperCase(Locale.ROOT), bulkRequest.getBody());
            IntermediateResponse subResponse = new IntermediateResponse();
            subResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

            BulkSubRequestRuntime runtime = new BulkSubRequestRuntime(container, subRequest, subResponse, userName);
            runtime.executeScript();
            if (runtime.getFailure() != null) {
                // A web script's transaction has already been rolled back when it throws.
                return failed(url, runtime.getFailure());
            }
            return toBulkSubResult(subResponse);
        } catch (Exception e) {
            return failed(url, e);
        } finally {
            // Restores the bulk caller's security context, even if the sub-request changed or cleared it.
            AuthenticationUtil.popAuthentication();
        }
    }

    /** Unexpected exceptions are reported as before: a 500 with the message in the body and no headers. */
    private static BulkSubResult failed(String url, Throwable e) {
        logger.error("Error in bulk call to {}", url, e);
        return new BulkSubResult(Status.STATUS_INTERNAL_SERVER_ERROR,
                BODY_MAPPER.valueToTree("Exception found: " + e.getMessage()), new HashMap<>());
    }

    /**
     * The body is parsed as JSON when possible and kept as a string otherwise; an empty body becomes {@code {}}. Each
     * header is reported with its first value.
     */
    static BulkSubResult toBulkSubResult(IntermediateResponse resp) throws IOException {
        Object body;
        String strBody = resp.getContentAsString();
        if (strBody == null || strBody.isEmpty()) {
            body = BODY_MAPPER.createObjectNode();
        } else {
            try {
                body = BODY_MAPPER.readTree(strBody);
            } catch (JsonParseException exception) {
                body = strBody;
            }
        }
        return new BulkSubResult(resp.getStatus(), body, resp.getHeaderNames().stream().collect(
                Collectors.toMap(Function.identity(), resp::getHeader)));
    }
}
