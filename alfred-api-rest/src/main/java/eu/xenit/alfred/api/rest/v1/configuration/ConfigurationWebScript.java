package eu.xenit.alfred.api.rest.v1.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.configuration.ConfigurationFileFlags;
import eu.xenit.alfred.api.configuration.ConfigurationService;
import eu.xenit.alfred.api.configuration.Configurations;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Returns the configuration files in a directory of the data dictionary, as JSON or, with {@code ?format=js} or
 * {@code Accept: application/js}, as a JSONP call of {@code callback}.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.configuration.configuration.get")
public class ConfigurationWebScript extends AbstractWebScript {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationWebScript.class);

    static final List<String> DEFAULT_FIELDS = List.of("content", "nodeRef");

    private final ConfigurationService configurationService;
    // The JSONP body used this plain mapper in the MVC implementation; kept so its output does not change.
    private final ObjectMapper jsonpMapper;

    public ConfigurationWebScript(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        jsonpMapper = new ObjectMapper();
        jsonpMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonpMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String searchDirectory = req.getParameter("searchDirectory");
        if (searchDirectory == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Required request parameter 'searchDirectory' is not present");
        }
        ConfigurationFileFlags flags = parseFields(req.getParameterValues("fields"));

        Configurations configurations;
        try {
            configurations = configurationService.getConfigurationFiles(searchDirectory,
                    req.getParameter("filter.name"), flags);
        } catch (IllegalArgumentException e) {
            // Replaces the @ExceptionHandler of the MVC implementation: 400 with the message as body.
            log.debug("Bad input", e);
            JsonResponses.writeText(res, Status.STATUS_BAD_REQUEST, String.valueOf(e.getMessage()));
            return;
        }

        if (isJsonp(req.getFormat(), req.getHeader("Accept"))) {
            // Like the MVC implementation, a missing callback is written as "null(...)".
            JsonResponses.writeJavaScript(res, String.format("%s(%s)", req.getParameter("callback"),
                    jsonpMapper.writeValueAsString(configurations)));
            return;
        }
        JsonResponses.writeJson(res, configurations);
    }

    /**
     * Mirrors Spring's binding of {@code @RequestParam(defaultValue = "content,nodeRef") String[] fields}: values
     * are comma separated and trimmed, and a missing or empty parameter falls back to the default.
     */
    static ConfigurationFileFlags parseFields(String[] values) {
        List<String> fields = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                for (String field : value.split(",")) {
                    if (!field.trim().isEmpty()) {
                        fields.add(field.trim());
                    }
                }
            }
        }
        if (fields.isEmpty()) {
            fields = DEFAULT_FIELDS;
        }
        return new ConfigurationFileFlags(
                fields.contains("content"),
                fields.contains("path"),
                fields.contains("parsedContent"),
                fields.contains("metadata"),
                fields.contains("nodeRef"));
    }

    /** JSONP when the format is "js" ({@code ?format=js}) or the Accept header's subtype is "js". */
    static boolean isJsonp(String format, String accept) {
        if ("js".equalsIgnoreCase(format)) {
            return true;
        }
        if (accept != null) {
            String[] acceptSplit = accept.split("/");
            return acceptSplit.length > 1 && "js".equalsIgnoreCase(acceptSplit[1]);
        }
        return false;
    }
}