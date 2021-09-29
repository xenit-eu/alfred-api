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
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


public class DocumentationWebscriptTest {

    @Test
    public void TestGenerate() throws JsonProcessingException {
        String swagText = Json.mapper().writeValueAsString(generateSwagger());
        System.out.println(swagText);
        Assert.assertTrue(0 < swagText.length());
    }

    @Test
    public void TestPermissionsMap() {
        Swagger swag = generateSwagger();
        Path path = swag.getPath("/v1/nodes/{space}/{store}/{guid}/permissions");
        Property schema = path.getGet().getResponses().get("200").getSchema();
        Assert.assertTrue("Nodes permissions get should return a map", schema instanceof MapProperty);
        System.out.println(schema);
    }

    private Swagger generateSwagger() {
        IVersionService version = mock(IVersionService.class);
        IWebUtils webUtils = mock(IWebUtils.class);
        when(version.getVersionDescription()).thenReturn(new VersionDescription("1.0.unittest", "description"));
        when(webUtils.getHost()).thenReturn("http");
        DocumentationWebscript web = new DocumentationWebscript(version, webUtils);
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
