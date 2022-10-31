package eu.xenit.apix.rest.v1.properties;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated Use DictionaryWebScript1 instead
 */
@AlfrescoAuthentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.property.PropertiesWebScript1")
public class PropertiesWebScript1 extends ApixV1Webscript {

    private final IPropertyService propertyService;

    public PropertiesWebScript1(IPropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping(value = "/v1/properties/{qname}")
    @ApiOperation(value = "Return the definition of a property", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = PropertyDefinition.class))
    //Use qname with slash to avoid
    //https://stackoverflow.com/questions/13482020/encoded-slash-2f-with-spring-requestmapping-path-param-gives-http-400
    public ResponseEntity<?> getPropertyDefinition(@PathVariable final QName qname,
                                      @RequestParam(required = false) QName qnameWithSlash) {
        // TODO @Zlatin FIXME Alfresco MVC some crappy URL shenanigans ? Unit-tested?
        QName apixQName = qnameWithSlash != null ? qnameWithSlash : qname;
        PropertyDefinition propDef = propertyService.GetPropertyDefinition(apixQName);
        if (propDef == null) {
            return ResponseEntity.notFound().build();
        }
        return writeJsonResponse(propDef);
    }
}
