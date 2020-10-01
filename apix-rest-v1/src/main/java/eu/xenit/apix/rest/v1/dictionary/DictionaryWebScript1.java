package eu.xenit.apix.rest.v1.dictionary;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.FormatStyle;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.IDictionaryService;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.dictionary.aspects.Aspects;
import eu.xenit.apix.dictionary.namespaces.Namespaces;
import eu.xenit.apix.dictionary.types.TypeDefinition;
import eu.xenit.apix.dictionary.types.Types;
import eu.xenit.apix.properties.Properties;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV1Config.BaseUrl + "/dictionary", families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves Dictionary information", value = "Dictionary")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.property.DictionaryWebScript1")
public class DictionaryWebScript1 extends ApixV1Webscript {

    Logger logger = LoggerFactory.getLogger(DictionaryWebScript1.class);
    @Autowired
    IDictionaryService dictionaryService;


    @Uri(value = "/properties/{qname}", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return the definition of a property", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = PropertyDefinition.class))
    public void getPropertyDefinition(@UriVariable final String qname, WebScriptResponse webScriptResponse)
            throws IOException {
        eu.xenit.apix.data.QName apixQName = new eu.xenit.apix.data.QName(qname);
        PropertyDefinition propDef = dictionaryService.GetPropertyDefinition(apixQName);
        if (propDef == null) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
        }
        writeJsonResponse(webScriptResponse, propDef);
    }


    @Uri(value = "/properties", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return properties", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Properties.class))
    public void getProperties(
            WebScriptResponse webScriptResponse) throws IOException {
        Properties properties = dictionaryService.getProperties();
        writeJsonResponse(webScriptResponse, properties);
    }


    @Uri(value = "/types", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return the definitions of types", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Types.class))
    public void getSubTypeDefinitions(@RequestParam(defaultValue = "sys:base", required = false) final String parent,
            WebScriptResponse webScriptResponse) throws IOException {
        QName apixQName = new QName(parent);
        Types types = dictionaryService.GetSubTypeDefinitions(apixQName, true);
        writeJsonResponse(webScriptResponse, types);
    }

    @Uri(value = "/types/{qname}", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return the definition of a type", notes = "")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = TypeDefinition.class),
            @ApiResponse(code = 404, message = "Not Found")})
    public void getTypeDefinition(@UriVariable final String qname, WebScriptResponse webScriptResponse)
            throws IOException {
        logger.debug("Received type qname %s", qname);
        eu.xenit.apix.data.QName apixQName = new eu.xenit.apix.data.QName(qname);
        TypeDefinition classDef = dictionaryService.GetTypeDefinition(apixQName);
        if (classDef == null) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
        }
        writeJsonResponse(webScriptResponse, classDef);
    }

    @Uri(value = "/aspects/{qname}", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return the definition of a aspect", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = AspectDefinition.class))
    public void getAspectDefinition(@UriVariable final String qname, WebScriptResponse webScriptResponse)
            throws IOException {
        logger.debug("Received aspect qname %s", qname);
        eu.xenit.apix.data.QName apixQName = new eu.xenit.apix.data.QName(qname);
        AspectDefinition classDef = dictionaryService.GetAspectDefinition(apixQName);
        if (classDef == null) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
        }
        writeJsonResponse(webScriptResponse, classDef);
    }


    @Uri(value = "/aspects", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Return apects", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Aspects.class))
    public void getAspects(WebScriptResponse webScriptResponse) throws IOException {
        Aspects aspects = dictionaryService.getAspects();
        writeJsonResponse(webScriptResponse, aspects);
    }

    @Uri(value = "/namespaces", method = HttpMethod.GET, formatStyle = FormatStyle.ARGUMENT)
    @ApiOperation(value = "Returns the namespaces", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Namespaces.class))
    public void getNamespaces(WebScriptResponse webScriptResponse) throws IOException {
        Namespaces namespaces = dictionaryService.getNamespaces();
        writeJsonResponse(webScriptResponse, namespaces);
    }


}
