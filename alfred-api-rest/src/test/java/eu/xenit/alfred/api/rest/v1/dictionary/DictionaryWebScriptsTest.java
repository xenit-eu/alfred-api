package eu.xenit.alfred.api.rest.v1.dictionary;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.dictionary.aspects.AspectDefinition;
import eu.xenit.alfred.api.dictionary.types.TypeDefinition;
import eu.xenit.alfred.api.dictionary.types.Types;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class DictionaryWebScriptsTest {

    private static final String FULL_QNAME = "{http://www.alfresco.org/model/content/1.0}name";

    private final IDictionaryService dictionaryService = mock(IDictionaryService.class);

    @Test
    void propertyLookupAcceptsFullQNameContainingSlashes() throws IOException {
        when(dictionaryService.GetPropertyDefinition(new QName(FULL_QNAME))).thenReturn(new PropertyDefinition());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        execute(new DictionaryPropertyWebScript(dictionaryService), requestWithQName(FULL_QNAME), res);

        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("{"));
    }

    @Test
    void missingPropertyIsEmpty404() throws IOException {
        assertEmpty404(new DictionaryPropertyWebScript(dictionaryService));
    }

    @Test
    void missingTypeIsEmpty404() throws IOException {
        assertEmpty404(new DictionaryTypeWebScript(dictionaryService));
    }

    @Test
    void missingAspectIsEmpty404() throws IOException {
        assertEmpty404(new DictionaryAspectWebScript(dictionaryService));
    }

    @Test
    void typeAndAspectLookupsUseTheTemplateQName() throws IOException {
        when(dictionaryService.GetTypeDefinition(new QName("cm:cmobject"))).thenReturn(new TypeDefinition());
        when(dictionaryService.GetAspectDefinition(new QName("cm:complianceable"))).thenReturn(new AspectDefinition());

        WebScriptResponse typeRes = responseWritingTo(new ByteArrayOutputStream());
        execute(new DictionaryTypeWebScript(dictionaryService), requestWithQName("cm:cmobject"), typeRes);
        verify(typeRes).setStatus(200);

        WebScriptResponse aspectRes = responseWritingTo(new ByteArrayOutputStream());
        execute(new DictionaryAspectWebScript(dictionaryService), requestWithQName("cm:complianceable"), aspectRes);
        verify(aspectRes).setStatus(200);
    }

    @Test
    void typesDefaultToSysBaseWhenParentIsMissingOrEmpty() throws IOException {
        when(dictionaryService.GetSubTypeDefinitions(any(), any(Boolean.class))).thenReturn(new Types());
        DictionaryTypesWebScript webScript = new DictionaryTypesWebScript(dictionaryService);

        execute(webScript, requestWithParent(null), responseWritingTo(new ByteArrayOutputStream()));
        execute(webScript, requestWithParent(""), responseWritingTo(new ByteArrayOutputStream()));
        execute(webScript, requestWithParent("cm:content"), responseWritingTo(new ByteArrayOutputStream()));

        verify(dictionaryService, org.mockito.Mockito.times(2)).GetSubTypeDefinitions(new QName("sys:base"), true);
        verify(dictionaryService).GetSubTypeDefinitions(new QName("cm:content"), true);
    }

    private void assertEmpty404(AbstractWebScript webScript) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);

        execute(webScript, requestWithQName("cm:foobar"), res);

        verify(res).setStatus(404);
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).setContentType(anyString());
        verify(res, never()).getOutputStream();
        verify(res, never()).getWriter();
    }

    private static void execute(AbstractWebScript webScript, WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        webScript.execute(req, res);
    }

    private static WebScriptRequest requestWithQName(String qname) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/dictionary/x/{qname}", Map.of("qname", qname),
                "/apix/v1/dictionary/x/"));
        return req;
    }

    private static WebScriptRequest requestWithParent(String parent) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("parent")).thenReturn(parent);
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}
