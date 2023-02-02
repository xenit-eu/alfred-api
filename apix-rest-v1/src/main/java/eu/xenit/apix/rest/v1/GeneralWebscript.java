package eu.xenit.apix.rest.v1;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.GeneralWebscript")
public class GeneralWebscript extends ApixV1Webscript {

    private final IVersionService versionService;

    public GeneralWebscript(IVersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping(value = "/v1/version")
    public ResponseEntity<VersionDescription> getApixVersion() {
        return writeJsonResponse(versionService.getVersionDescription());
    }
}