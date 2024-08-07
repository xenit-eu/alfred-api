package eu.xenit.apix.rest.v1.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import eu.xenit.apix.configuration.ConfigurationFileFlags;
import eu.xenit.apix.configuration.ConfigurationService;
import eu.xenit.apix.configuration.Configurations;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class ConfigurationWebscript1 extends ApixV1Webscript {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationWebscript1.class);

    private final ObjectMapper mapper;


    private final ConfigurationService configurationService;

    public ConfigurationWebscript1(
            @Qualifier("eu.xenit.apix.configuration.ConfigurationService") ConfigurationService configurationService) {
        this.configurationService = configurationService;
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/configuration", produces = {"application/js", MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getJsConfigurationFiles(
            @RequestParam(defaultValue = "content,nodeRef", required = false) String[] fields,
            @RequestParam String searchDirectory,
            @RequestParam(value = "filter.name", required = false) String nameFilter,
            @RequestParam(required = false) String callback, final HttpServletRequest req
    ) throws IOException {
        final WebScriptRequest wsReq = ((DispatcherWebscript.WebscriptRequestWrapper) req).getWebScriptServletRequest();
        List<String> fieldsList = Arrays.asList(fields);
        ConfigurationFileFlags configurationFileFlags = new ConfigurationFileFlags(
                fieldsList.contains("content"),
                fieldsList.contains("path"),
                fieldsList.contains("parsedContent"),
                fieldsList.contains("metadata"),
                fieldsList.contains("nodeRef"));
        Configurations configurations = configurationService
                .getConfigurationFiles(searchDirectory, nameFilter, configurationFileFlags);
        if ("js".equalsIgnoreCase(wsReq.getFormat()) ||
                "js".equalsIgnoreCase(
                        getAcceptSubType(
                                req.getHeader("Accept")))) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("application", "js"))
                    .body(
                            String.format("%s(%s)", callback,
                                    mapper.writeValueAsString(configurations)));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(configurations);
    }

    @Nullable
    private static String getAcceptSubType(String accept) {
        String acceptSubType = null;
        if (accept != null) {
            String[] acceptSplit = accept.split("/");
            if (acceptSplit.length > 1) {
                acceptSubType = acceptSplit[1];
            }
        }
        return acceptSubType;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<?> writeBadRequestResponse(IllegalArgumentException exception) {
        log.debug("Bad input", exception);
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
