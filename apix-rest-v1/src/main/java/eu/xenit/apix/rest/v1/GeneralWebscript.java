package eu.xenit.apix.rest.v1;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import eu.xenit.apix.web.IWebUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV1Config.BaseUrl, families = {RestV1Config.Family}, defaultFormat = "json",
        description = "General API operations", value = "General")
@Component("eu.xenit.apix.rest.v1.GeneralWebscript")
@Authentication(AuthenticationType.USER)
public class GeneralWebscript extends ApixV1Webscript {//implements BeanFactoryAware{

    Logger logger = LoggerFactory.getLogger(GeneralWebscript.class);

    IVersionService versionService;

    @Autowired
    public GeneralWebscript(IVersionService versionService, IWebUtils webUtils) {
        this.versionService = versionService;
    }

    @Uri(value = "/version", method = HttpMethod.GET)
    @ApiOperation("Access the version information for Api-X")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = VersionDescription.class))
    public void getApixVersion(WebScriptResponse response) throws IOException {
        writeJsonResponse(response, versionService.getVersionDescription());
    }

}