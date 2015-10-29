package eu.xenit.apix.rest.v1.bulk;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.WebScriptUriRegistry;
import com.github.dynamicextensionsalfresco.webscripts.annotations.FormatStyle;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by kenneth on 18.03.16. Largely rewritten by roel on 13.03.18
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Perform multiple operations in a single call", value = "Bulk")
@Component("eu.xenit.apix.rest.v1.BulkWebscript")
public class BulkWebscript1 extends ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(BulkWebscript1.class);
    @Autowired
    ServiceRegistry serviceRegistry;
    // Dynamic Extensions is needed to provide the object that can be autowired in here
    @Autowired
    WebScriptUriRegistry wsuriRegistry;

    public BulkWebscript1() {
    }

    public BulkWebscript1(ServiceRegistry serviceRegistry, WebScriptUriRegistry registry) {
        this.serviceRegistry = serviceRegistry;
        this.wsuriRegistry = registry;
    }

    public static BulkSubResult createBulkResult(ObjectMapper mapper, IntermediateResponse resp) throws IOException {
        int status = resp.getStatus();
        JsonNode body;
        String strbody = resp.getWriter().toString();

        if (strbody == null || strbody.equals("")) {
            body = mapper.createObjectNode();
        } else {
            body = mapper.readTree(resp.getWriter().toString());
        }

        return new BulkSubResult(status, body, resp.getHeaders());
    }

    public static BulkSubResult create404subResult(ObjectMapper mapper, String url) throws IOException {
        return new BulkSubResult(HttpStatus.SC_NOT_FOUND /* = 404 */,
                mapper.readTree("\"No Webscript found for url " +
                        new String(JsonStringEncoder.getInstance().quoteAsString(url)) + "\""),
                new HashMap<String, String>(0));
    }

    @Uri(value = "/bulk", method = HttpMethod.POST, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation("Performs multiple Api-X operations in a single rest call")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = BulkSubResult[].class))
    public void bulk(final BulkRequest[] bulkRequests, final WebScriptRequest req, final WebScriptResponse response)
            throws IOException {

        final WebScriptUriRegistry registry = wsuriRegistry;
        final ObjectMapper mapper = new ObjectMapper();
        final List<BulkSubResult> bulkResults = new ArrayList<>();

        for (int i = 0; i < bulkRequests.length; i++) {
            BulkRequest request = bulkRequests[i];
            // v1 bulk only gets to send to other v1 endpoints :(
            // We need to url decode this so we can find the match and parse any @UriVariable
            final String url = URLDecoder.decode("/apix/v1" + request.getUrl());

            logger.debug("Evaluating {} with body {}", url, request.getBody());

            String strippedUrl = url;
            if(url.contains("?")) {
                strippedUrl = url.substring(0, url.indexOf("?"));
            }

            //The Match created in this method does not split off the querystring arguments correctly,
            //therefore we will remove the queryString from the url for this method. Later in the call,
            //the url with query string is still passed to have it handled.
            final Match m = registry.findWebScript(request.getMethod(), strippedUrl);

            if (m == null || m.getWebScript() == null) {
                logger.debug("Could not find any webscript bound to {}. Injecting 404 subresponse.", url);
                bulkResults.add(create404subResult(mapper, url));
                continue;
            }

            logger.debug("Matched on {}", m.getWebScript().getClass().toString());

            // These Intermediate... classes are simple container objects, standing in for HTTP requests and responses
            // They are used to execute the WebScript directly, providing arguments and retrieving the result
            Content intermediateContent = new IntermediateContent(req.getContent(), request.getBody());
            final WebScriptRequest intermediateRequest = new IntermediateRequest(req, url, intermediateContent, m);
            final IntermediateResponse intermediateResponse = new IntermediateResponse(req.getRuntime());

            // Each subrequest gets to run in its own transaction context, because even a caught exception can mark a
            // transaction as "needs to be rolled back". We don't want a previous subrequest to be rolled back because
            // the next subrequest used some exception-driven code. Also, please don't use exceptions as flow control.
            BulkSubResult subRes = (BulkSubResult) serviceRegistry.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                        @Override
                        public Object execute() throws Throwable {
                            try {
                                // Actual execution of called WebScript happens here
                                m.getWebScript().execute(intermediateRequest, intermediateResponse);
                                logger.debug("Resp body is '{}'", intermediateResponse.getWriter().toString());
                                // Convert the response container to json + statuscode + headers
                                return createBulkResult(mapper, intermediateResponse);
                            } catch (Exception e) {
                                // Catching all exceptions to just print a stacktrace isn't super clean...
                                // But we want the bulk to continue with the other requests even if this one fails.
                                logger.error("Error in bulk call to " + url, e);
                                return new BulkSubResult(500,
                                        new ObjectMapper().valueToTree("Exception found: " + e.getMessage()),
                                        new HashMap<String, String>());
                            }
                        }
                    }, false, true);

            if (subRes == null) {
                logger.warn("bulkSubResult is null for request {}.", url);
            } else {
                bulkResults.add(subRes);
            }
        }

        if (bulkResults.size() > 0) {
            // Write out the list of BulkSubResults which becomes nice json
            writeJsonResponse(response, bulkResults);
        } else {
            logger.warn("No results for bulk operation");
        }
    }
}
