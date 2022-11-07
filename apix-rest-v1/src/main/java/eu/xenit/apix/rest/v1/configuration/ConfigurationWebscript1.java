package eu.xenit.apix.rest.v1.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.ExceptionHandler;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.configuration.ConfigurationFileFlags;
import eu.xenit.apix.configuration.ConfigurationService;
import eu.xenit.apix.configuration.Configurations;
import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves configuration data files from the datadictionary", value = "Configuration")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.configuration.ConfigurationWebscript1")
public class ConfigurationWebscript1 extends ApixV1Webscript {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationWebscript1.class);

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    INodeService nodeService;

    @Autowired
    IContentService contentService;

    @Autowired
    ConfigurationService configurationService;


    @Uri(value = "/configuration", method = HttpMethod.GET, defaultFormat = "json")

    @ApiOperation("Returns configuration files information and content")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Configurations.class))

    public void getConfigurationFiles(
            @RequestParam(defaultValue = "content,nodeRef", delimiter = ",") @ApiParam(value = "Comma separated field names to include.", defaultValue = "content,nodeRef", allowableValues = "content,nodeRef,path,metadata,parsedContent") String[] fields,
            @RequestParam @ApiParam("The directory to search for configuration files, relative to the data dictionary") String searchDirectory,
            @RequestParam(value = "filter.name", required = false) @ApiParam(name = "filter.name", value = "Regular expression that the node name should match.") String nameFilter,
            @RequestParam(required = false) @ApiParam("Javascript callback function") String callback,
            WebScriptRequest webScriptRequest,
            WebScriptResponse webScriptResponse
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

        if (webScriptRequest.getFormat().equalsIgnoreCase("js")) {
            writeJsResponse(callback, webScriptResponse, configurations);
        } else {
            writeJsonResponse(webScriptResponse, configurations);
        }
    }

    private void writeJsResponse(String callback, WebScriptResponse webScriptResponse, Configurations configurations)
            throws IOException {
        webScriptResponse.setContentType("application/js");
        webScriptResponse.setContentEncoding("utf-8");
        webScriptResponse.setHeader("Cache-Control", "no-cache");
        Writer writer = webScriptResponse.getWriter();
        writer.write(callback);
        writer.write("(");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.writeValue(writer, configurations);
        writer.write(");");
        writer.flush();
        writer.close();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private void writeBadRequestResponse(IllegalArgumentException exception, WebScriptResponse response)  throws IOException{
        log.debug("Bad input;", exception);
        response.setStatus(400);
        writeJsonResponse(response, exception.getMessage());
    }
}
