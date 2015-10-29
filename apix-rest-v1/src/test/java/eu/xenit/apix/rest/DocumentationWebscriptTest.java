package eu.xenit.apix.rest;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.apix.rest.v1.DocumentationWebscript;
import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import eu.xenit.apix.web.IWebUtils;
import io.swagger.models.Swagger;
import io.swagger.models.properties.MapProperty;
import io.swagger.util.Json;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Created by Michiel Huygen on 12/05/2016.
 */
public class DocumentationWebscriptTest {

    @Test
    public void TestGenerate() throws JsonProcessingException {
        io.swagger.models.Swagger swag = generateSwagger();

        System.out.println(Json.mapper().writeValueAsString(swag));


    }

    @Test
    public void TestPermissionsMap() {
        Swagger swag = generateSwagger();
        io.swagger.models.Path path = swag.getPath("/v1/nodes/{space}/{store}/{guid}/permissions");
        io.swagger.models.properties.Property schema = path.getGet().getResponses().get("200").getSchema();
        Assert.assertTrue("Nodes permissions get should return a map", schema instanceof MapProperty);
        System.out.println(schema);

    }

    private Swagger generateSwagger() {
        IVersionService version = mock(IVersionService.class);
        IWebUtils webutils = mock(IWebUtils.class);
        when(version.getVersionDescription()).thenReturn(new VersionDescription("1.0.unittest", "description"));
        when(webutils.getHost()).thenReturn("http");
        DocumentationWebscript web = new DocumentationWebscript(version, webutils);
        return web.generateSwagger();
    }

    @Test
    public void testRedirectToSwaggerUi() {
        WebScriptRequest request = mock(WebScriptRequest.class);
        String serviceContextPath = "https://testdomain.com/alfresco/service";
        when(request.getServiceContextPath()).thenReturn(serviceContextPath);
        when(request.getServicePath()).thenReturn(serviceContextPath.concat("/apix/v1/docs/ui"));
        WebScriptResponse response = mock(WebScriptResponse.class);
        DocumentationWebscript documentationWebscript = new DocumentationWebscript(mock(IVersionService.class),
                mock(IWebUtils.class));
        documentationWebscript.redirectToSwaggerUi(request, response);
        verify(response).setStatus(302);
        verify(response).setHeader(eq("Location"),
                eq("https://testdomain.com/alfresco/service/swagger/ui/?url=" + URLEncoder
                        .encode("https://testdomain.com/alfresco/service/apix/v1/docs/swagger.json")));
    }
}
