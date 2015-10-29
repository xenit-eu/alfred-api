package io.swagger.sample.reader;

import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import io.swagger.annotations.*;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

@SwaggerDefinition(
        info = @Info(
                description = "This is the swagger specification for a TEST moehahaha",
                version = "1.33.7",
                title = "Swagger Dynamic Extensions Test",
                //termsOfService = "http://swagger.io/terms/",
                contact = @Contact(name = "XeniT", email = "apix@xenit.eu", url = "http://www.xenit.eu")
                //license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        //consumes = {"application/json", "application/xml"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {@Tag(name = "core", description = "Core Operations")}
)
@WebScript(description = "Webscript description", baseUri = "/de")
public class DEWebscript {

    @Uri("/home")
    @ApiOperation(value = "/home summary", tags = "core", notes = "/home description")
    public SampleModel simpleGet(@RequestParam String requestParam,
            @RequestParam(required = false) String requiredParam,
            @UriVariable String pathParam,
            SampleModel bodyParam) {
        return new SampleModel();
    }

    @Uri("/ignoreParams")
    public void ignoreParams(WebScriptRequest request, WebScriptResponse responose) {

    }
}
