package eu.xenit.alfred.api.rest.v1.dictionary;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.dictionary.aspects.AspectDefinition;
import eu.xenit.alfred.api.dictionary.aspects.Aspects;
import eu.xenit.alfred.api.dictionary.namespaces.Namespaces;
import eu.xenit.alfred.api.dictionary.types.TypeDefinition;
import eu.xenit.alfred.api.dictionary.types.Types;
import eu.xenit.alfred.api.properties.Properties;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class DictionaryWebScript1 extends AlfredApiV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryWebScript1.class);
    private final IDictionaryService dictionaryService;

    public DictionaryWebScript1(
            @Qualifier(("eu.xenit.alfred.api.dictionary.IDictionaryService")) IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/properties/**")
    public ResponseEntity<?> getPropertyDefinition(HttpServletRequest request) {
        QName qname = extractQNameFromUrlPath(request, "/v1/dictionary/properties/");
        PropertyDefinition propDef = dictionaryService.GetPropertyDefinition(qname);
        if (propDef == null) {
            return ResponseEntity.notFound().build();
        }
        return writeJsonResponse(propDef);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/properties")
    public ResponseEntity<Properties> getProperties() {
        return writeJsonResponse(dictionaryService.getProperties());
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/types")
    public ResponseEntity<Types> getSubTypeDefinitions(
            @RequestParam(defaultValue = "sys:base", required = false) final String parent) {
        return writeJsonResponse(
                dictionaryService.GetSubTypeDefinitions(
                        new QName(parent), true
                )
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/types/**")
    public ResponseEntity<?> getTypeDefinition(HttpServletRequest request) {
        QName qname = extractQNameFromUrlPath(request, "/v1/dictionary/types/");
        logger.debug("Received type qname {}", qname);
        TypeDefinition classDef = dictionaryService.GetTypeDefinition(qname);
        if (classDef == null) {
            return ResponseEntity.notFound().build();
        }
        return writeJsonResponse(classDef);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/aspects/**")
    public ResponseEntity<?> getAspectDefinition(HttpServletRequest request) {
        QName qname = extractQNameFromUrlPath(request, "/v1/dictionary/aspects/");
        logger.debug("Received aspect qname {}", qname);
        AspectDefinition classDef = dictionaryService.GetAspectDefinition(qname);
        if (classDef == null) {
            return ResponseEntity.notFound().build();
        }
        return writeJsonResponse(classDef);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/aspects")
    public ResponseEntity<Aspects> getAspects() {
        return writeJsonResponse(dictionaryService.getAspects());
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/dictionary/namespaces")
    public ResponseEntity<Namespaces> getNamespaces() {
        return writeJsonResponse(dictionaryService.getNamespaces());
    }

    private QName extractQNameFromUrlPath(HttpServletRequest request, String path) {
        String qnameValue = request.getRequestURI().split(request.getContextPath() + path)[1];
        return new QName(qnameValue);
    }
}
