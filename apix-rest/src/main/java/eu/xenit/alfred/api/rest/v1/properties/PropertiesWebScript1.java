package eu.xenit.alfred.api.rest.v1.properties;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.properties.IPropertyService;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated since Oct 2015, use DictionaryWebScript1 instead
 */
@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
@Deprecated(since = "Deprecated since Oct 2015, use DictionaryWebScript1 instead")
public class PropertiesWebScript1 extends ApixV1Webscript {

    private final IPropertyService propertyService;

    public PropertiesWebScript1(IPropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/properties/{qname}")
    //Use qname with slash to avoid
    //https://stackoverflow.com/questions/13482020/encoded-slash-2f-with-spring-requestmapping-path-param-gives-http-400
    public ResponseEntity<?> getPropertyDefinition(@PathVariable final QName qname,
            @RequestParam(required = false) QName qnameWithSlash) {
        QName apixQName = qnameWithSlash != null ? qnameWithSlash : qname;
        PropertyDefinition propDef = propertyService.GetPropertyDefinition(apixQName);
        if (propDef == null) {
            return ResponseEntity.notFound().build();
        }
        return writeJsonResponse(propDef);
    }
}
