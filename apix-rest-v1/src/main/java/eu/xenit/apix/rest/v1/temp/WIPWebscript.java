package eu.xenit.apix.rest.v1.temp;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.WIP.IWIPService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 27/05/2016.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Work In Progress - UNSTABLE", value = "WIP")
@Component("eu.xenit.apix.rest.v1.temp.WIPWebscript")
public class WIPWebscript extends ApixV1Webscript {

    @Autowired
    IWIPService WipService;

    @ApiOperation(value = "Downloads preview file for given node")
    @Uri(value = "/nodes/{space}/{store}/{guid}/content/previews/pdf", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiImplicitParams({@ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = true)})
    public void getPreviewPdf(@UriVariable String space, @UriVariable String store, @UriVariable String guid,
            final WebScriptRequest multiPart, WebScriptResponse response) throws IOException {
        final NodeRef nodeRef = new NodeRef(space, store, guid);
        //TODO: from /searchapp/download
    }
}
