package eu.xenit.alfred.api.rest.v1;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.version.IVersionService;
import eu.xenit.alfred.api.version.VersionDescription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class GeneralWebscript extends AlfredApiV1Webscript {

    private final IVersionService versionService;

    public GeneralWebscript(IVersionService versionService) {
        this.versionService = versionService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/version")
    public ResponseEntity<VersionDescription> getAlfredApiVersion() {
        return writeJsonResponse(versionService.getVersionDescription());
    }
}