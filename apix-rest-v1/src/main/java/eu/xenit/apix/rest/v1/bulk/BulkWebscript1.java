package eu.xenit.apix.rest.v1.bulk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.bulk.request.BulkHttpServletRequest;
import eu.xenit.apix.rest.v1.bulk.request.BulkRequest;
import eu.xenit.apix.rest.v1.bulk.request.IntermediateRequest;
import eu.xenit.apix.rest.v1.bulk.response.IntermediateResponse;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController("eu.xenit.apix.rest.v1.BulkWebscript")
public class BulkWebscript1 extends ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(BulkWebscript1.class);

    private final ServiceRegistry serviceRegistry;
    final ObjectMapper mapper = new ObjectMapper();
    private final DispatcherWebscript dispatcherWebscript;

    public BulkWebscript1(ServiceRegistry serviceRegistry,
                          @Qualifier("alfred.api") DispatcherWebscript dispatcherWebscript) {
        this.serviceRegistry = serviceRegistry;
        this.dispatcherWebscript = dispatcherWebscript;
    }

    @PostMapping(value = "/v1/bulk")
    public ResponseEntity<List<BulkSubResult>> bulk(@RequestBody final BulkRequest[] bulkRequests,
                                                    final HttpServletRequest req) {
        final WebScriptRequest wsReq = ((DispatcherWebscript.WebscriptRequestWrapper) req).getWebScriptServletRequest();

        final List<BulkSubResult> bulkResults = new ArrayList<>();

        for (BulkRequest request : bulkRequests) {
            // v1 bulk only gets to send to other v1 endpoints :(
            // We need to url decode this, so we can find the match and parse any @UriVariable

            BulkHttpServletRequest bulkHttpServletRequest = new BulkHttpServletRequest(req,
                    URLDecoder.decode("/alfresco/service/apix/v1" + request.getUrl()),
                    request.getMethod().toUpperCase(), request.getBody());
            logger.debug("Evaluating {} with body {} and method {}", bulkHttpServletRequest.getRequestURI(),
                    bulkHttpServletRequest.getBody(),
                    bulkHttpServletRequest.getMethod());


            final IntermediateRequest intermediateRequest = new IntermediateRequest(wsReq, bulkHttpServletRequest,
                    null);

            final IntermediateResponse mockRes = new IntermediateResponse();
            final WebScriptServletResponse intermediateResponse = new WebScriptServletResponse(wsReq.getRuntime(),
                    mockRes);


            // Each subrequest gets to run in its own transaction context, because even a caught exception can mark a
            // transaction as "needs to be rolled back". We don't want a previous subrequest to be rolled back because
            // the next subrequest used some exception-driven code. Also, please don't use exceptions as flow control.
            BulkSubResult subRes = (BulkSubResult) serviceRegistry.getRetryingTransactionHelper()
                    .doInTransaction((RetryingTransactionHelper.RetryingTransactionCallback<Object>) () -> {
                        try {
                            // Actual execution of called WebScript happens here
                            dispatcherWebscript.execute(intermediateRequest, intermediateResponse);
                            logger.debug("Resp body is '{}'", intermediateResponse.getWriter());
                            // Convert the response container to json + statuscode + headers
                            return createBulkResult(mapper, mockRes);
                        } catch (Exception e) {
                            // Catching all exceptions to just print a stacktrace isn't super clean...
                            // But we want the bulk to continue with the other requests even if this one fails.
                            logger.error("Error in bulk call to {}", intermediateRequest.getHttpServletRequest().getRequestURI(), e);
                            return new BulkSubResult(500,
                                    mapper.valueToTree("Exception found: " + e.getMessage()),
                                    new HashMap<>());
                        }
                    }, false, true);

            if (subRes == null) {
                logger.warn("bulkSubResult is null for request {}.", intermediateRequest.getHttpServletRequest().getRequestURI());
            } else {
                bulkResults.add(subRes);
            }
        }

        if (!bulkResults.isEmpty()) {
            // Write out the list of BulkSubResults which becomes nice json
            return writeJsonResponse(bulkResults);
        }
        logger.warn("No results for bulk operation");
        return ResponseEntity.noContent().build();
    }

    private static BulkSubResult createBulkResult(ObjectMapper mapper, IntermediateResponse resp) throws IOException {
        int status = resp.getStatus();
        JsonNode body;
        String strBody = resp.getContentAsString();
        if (strBody == null || strBody.equals("")) {
            body = mapper.createObjectNode();
        } else {
            body = mapper.readTree(strBody);
        }

        return new BulkSubResult(status, body, resp.getHeaderNames().stream().collect(
                Collectors.toMap(Function.identity(), resp::getHeader)
        ));
    }

}
