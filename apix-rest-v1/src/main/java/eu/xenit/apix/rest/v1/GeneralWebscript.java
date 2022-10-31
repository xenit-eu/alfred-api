package eu.xenit.apix.rest.v1;

import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@Authentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.GeneralWebscript")
public class GeneralWebscript extends ApixV1Webscript {

    private final IVersionService versionService;

    public GeneralWebscript(IVersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping(value = "/v1/version")
    @ApiOperation("Access the version information for Api-X")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = VersionDescription.class))
    public ResponseEntity<VersionDescription> getApixVersion() {
        return writeJsonResponse(versionService.getVersionDescription());
    }
}