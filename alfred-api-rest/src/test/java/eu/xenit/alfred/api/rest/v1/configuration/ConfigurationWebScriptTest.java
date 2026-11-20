package eu.xenit.alfred.api.rest.v1.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.configuration.ConfigurationFile;
import eu.xenit.alfred.api.configuration.ConfigurationFileFlags;
import eu.xenit.alfred.api.configuration.ConfigurationService;
import eu.xenit.alfred.api.configuration.Configurations;
import eu.xenit.alfred.api.data.NodeRef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class ConfigurationWebScriptTest {

    private static final String EXPECTED_JSON =
            "{\"files\":[{\"content\":\"a: b\",\"nodeRef\":\"workspace://SpacesStore/config\"}]}";

    private final ConfigurationService configurationService = mock(ConfigurationService.class);

    @Test
    void fieldsAreBoundLikeSpringDidWithContentAndNodeRefAsDefault() {
        for (String[] missing : new String[][]{null, {""}, {" , "}}) {
            ConfigurationFileFlags flags = ConfigurationWebScript.parseFields(missing);
            assertTrue(flags.isAddContent() && flags.isAddNodeRef());
            assertFalse(flags.isAddPath() || flags.isAddParsedContent() || flags.isAddMetadata());
        }

        ConfigurationFileFlags flags = ConfigurationWebScript.parseFields(new String[]{"path, metadata"});
        assertTrue(flags.isAddPath() && flags.isAddMetadata());
        assertFalse(flags.isAddContent() || flags.isAddNodeRef() || flags.isAddParsedContent());
    }

    @Test
    void jsonpIsChosenByFormatOrAcceptHeader() {
        assertTrue(ConfigurationWebScript.isJsonp("js", null));
        assertTrue(ConfigurationWebScript.isJsonp("json", "application/js"));
        assertFalse(ConfigurationWebScript.isJsonp("json", "application/json"));
        assertFalse(ConfigurationWebScript.isJsonp(null, "*"));
    }

    @Test
    void writesJsonByDefaultAndJsonpWhenAsked() throws IOException {
        when(configurationService.getConfigurationFiles(eq("dir"), eq(null), any())).thenReturn(configurations());
        ConfigurationWebScript webScript = new ConfigurationWebScript(configurationService);

        ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
        WebScriptResponse jsonRes = responseWritingTo(jsonOut);
        webScript.execute(request("dir", "json", null), jsonRes);
        verify(jsonRes).setStatus(200);
        verify(jsonRes).setContentType("application/json");
        assertEquals(EXPECTED_JSON, jsonOut.toString(StandardCharsets.UTF_8));

        ByteArrayOutputStream jsOut = new ByteArrayOutputStream();
        WebScriptResponse jsRes = responseWritingTo(jsOut);
        webScript.execute(request("dir", "js", "cb"), jsRes);
        verify(jsRes).setStatus(200);
        verify(jsRes).setContentType("application/js");
        verify(jsRes).setHeader("Cache-Control", "no-cache");
        assertEquals("cb(" + EXPECTED_JSON + ")", jsOut.toString(StandardCharsets.UTF_8));
    }

    @Test
    void missingSearchDirectoryAndInvalidInputAre400() throws IOException {
        ConfigurationWebScript webScript = new ConfigurationWebScript(configurationService);

        WebScriptException e = assertThrows(WebScriptException.class,
                () -> webScript.execute(request(null, "json", null), mock(WebScriptResponse.class)));
        assertEquals(400, e.getStatus());
        verifyNoInteractions(configurationService);

        when(configurationService.getConfigurationFiles(eq("missing"), any(), any()))
                .thenThrow(new IllegalArgumentException("Path does not exist"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);
        webScript.execute(request("missing", "json", null), res);
        verify(res).setStatus(400);
        verify(res).setContentType("text/plain;charset=UTF-8");
        assertEquals("Path does not exist", out.toString(StandardCharsets.UTF_8));
    }

    private static Configurations configurations() {
        ConfigurationFile file = new ConfigurationFile();
        file.setContent("a: b");
        file.setNodeRef(new NodeRef("workspace://SpacesStore/config"));
        return new Configurations(List.of(file));
    }

    private static WebScriptRequest request(String searchDirectory, String format, String callback) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("searchDirectory")).thenReturn(searchDirectory);
        when(req.getParameter("callback")).thenReturn(callback);
        when(req.getFormat()).thenReturn(format);
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}