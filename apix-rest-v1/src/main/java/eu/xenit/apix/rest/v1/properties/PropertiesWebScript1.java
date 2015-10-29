package eu.xenit.apix.rest.v1.properties;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Created by Jasperhilven on 13-Jan-17.
 *
 * @deprecated Use DictionaryWebScript1 instead
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves Property information", value = "Properties")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.property.PropertiesWebScript1")
public class PropertiesWebScript1 extends ApixV1Webscript {

    Logger logger = LoggerFactory.getLogger(PropertiesWebScript1.class);

    @Autowired
    IPropertyService propertyService;


    @Uri(value = "/properties/{qname}", method = HttpMethod.GET)
    @ApiOperation(value = "Return the definition of a property", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = PropertyDefinition.class))
    //Use qname with slash to avoid
    //https://stackoverflow.com/questions/13482020/encoded-slash-2f-with-spring-requestmapping-path-param-gives-http-400
    public void getPropertyDefinition(@UriVariable final String qname,
            @RequestParam(required = false) String qnameWithSlash, WebScriptResponse webScriptResponse)
            throws IOException {
        String qnameUsed = qnameWithSlash != null ? qnameWithSlash : qname;
        String decoded = java.net.URLDecoder.decode(qnameUsed, "UTF-8");
        logger.debug("Asked versionhistory for node with guid: " + decoded);
        eu.xenit.apix.data.QName apixQName = new eu.xenit.apix.data.QName(qnameUsed);
        PropertyDefinition propDef = propertyService.GetPropertyDefinition(apixQName);
        if (propDef == null) {
            webScriptResponse.setStatus(HttpStatus.NOT_FOUND.value());
        }
        writeJsonResponse(webScriptResponse, propDef);
    }
}
