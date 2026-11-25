package eu.xenit.alfred.api.rest.v1.properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.properties.IPropertyService;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class PropertyWebScriptTest {

    private static final String FULL_QNAME = "{http://www.alfresco.org/model/content/1.0}name";

    private final IPropertyService propertyService = mock(IPropertyService.class);

    @Test
    void qnameWithSlashParameterTakesPrecedenceOverThePath() throws IOException {
        when(propertyService.GetPropertyDefinition(new QName(FULL_QNAME))).thenReturn(new PropertyDefinition());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);

        new PropertyWebScript(propertyService).execute(request("ignored", FULL_QNAME), res);

        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("{"));
    }

    @Test
    void missingPropertyIsEmpty404() throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);

        new PropertyWebScript(propertyService).execute(request("cm:foobar", null), res);

        verify(propertyService).GetPropertyDefinition(new QName("cm:foobar"));
        verify(res).setStatus(404);
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).setContentType(anyString());
        verify(res, never()).getOutputStream();
    }

    private static WebScriptRequest request(String pathQName, String qnameWithSlash) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/properties/{qname}", Map.of("qname", pathQName),
                "/apix/v1/properties/"));
        when(req.getParameter("qnameWithSlash")).thenReturn(qnameWithSlash);
        return req;
    }
}