package eu.xenit.apix.rest.v1.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.apix.configuration.ConfigurationFileFlags;
import eu.xenit.apix.configuration.ConfigurationService;
import eu.xenit.apix.configuration.Configurations;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.configuration.ConfigurationWebscript1")
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


    @GetMapping(value = "/v1/configuration", consumes = {"application/js"}, produces = {"application/js"})
    @ApiOperation("Returns configuration files information and content")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Configurations.class))
    public ResponseEntity<?> getJsConfigurationFiles(
            @RequestParam(defaultValue = "content,nodeRef", required = false)
            @ApiParam(
                    value = "Comma separated field names to include.",
                    defaultValue = "content,nodeRef",
                    allowableValues = "content,nodeRef,path,metadata,parsedContent") String[] fields,
            @RequestParam @ApiParam("The directory to search for configuration files, relative to the data dictionary")
            String searchDirectory,
            @RequestParam(value = "filter.name", required = false)
            @ApiParam(name = "filter.name", value = "Regular expression that the node name should match.")
            String nameFilter,
            @RequestParam(required = false) @ApiParam("Javascript callback function") String callback
    ) throws IOException {
        List<String> fieldsList = Arrays.asList(fields);
        ConfigurationFileFlags configurationFileFlags = new ConfigurationFileFlags(
                fieldsList.contains("content"),
                fieldsList.contains("path"),
                fieldsList.contains("parsedContent"),
                fieldsList.contains("metadata"),
                fieldsList.contains("nodeRef"));
        Configurations configurations = configurationService
                .getConfigurationFiles(searchDirectory, nameFilter, configurationFileFlags);
        return ResponseEntity.ok()
                .body(
                        String.format("%s(%s)", callback,
                                mapper.writeValueAsString(configurations)));
//        webScriptResponse.setContentEncoding("utf-8");
//        webScriptResponse.setHeader("Cache-Control", "no-cache");
//
//        if (webScriptRequest.getFormat().equalsIgnoreCase("js")) {
//            webScriptResponse.setContentType("application/js");
//            try(Writer writer = webScriptResponse.getWriter()) {
//                writer.write(callback);
//                writer.write("(");
//                mapper.writeValue(writer, configurations);
//                writer.write(");");
//                writer.flush();
//            }
//            return;
//        }
    }


    @GetMapping(value = "/v1/configuration" ,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Returns configuration files information and content")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Configurations.class))
    public ResponseEntity<?> getConfigurationFiles(
            @RequestParam(defaultValue = "content,nodeRef", required = false)
                @ApiParam(
                    value = "Comma separated field names to include.",
                    defaultValue = "content,nodeRef",
                    allowableValues = "content,nodeRef,path,metadata,parsedContent")
            String[] fields,
            @RequestParam
            @ApiParam("The directory to search for configuration files, relative to the data dictionary")
                String searchDirectory,
            @RequestParam(value = "filter.name", required = false)
                @ApiParam(name = "filter.name", value = "Regular expression that the node name should match.")
                String nameFilter
    ) throws IOException {
        List<String> fieldsList = Arrays.asList(fields);
        ConfigurationFileFlags configurationFileFlags = new ConfigurationFileFlags(
                fieldsList.contains("content"),
                fieldsList.contains("path"),
                fieldsList.contains("parsedContent"),
                fieldsList.contains("metadata"),
                fieldsList.contains("nodeRef"));
        Configurations configurations = configurationService
                .getConfigurationFiles(searchDirectory, nameFilter, configurationFileFlags);
        return ResponseEntity.ok()
                .body(configurations);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<?> writeBadRequestResponse(IllegalArgumentException exception) {
        log.debug("Bad input", exception);
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
