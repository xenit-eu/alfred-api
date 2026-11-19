package eu.xenit.alfred.api.rest.v1.versionhistory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import eu.xenit.alfred.api.versionhistory.Version;
import eu.xenit.alfred.api.versionhistory.Version.VersionType;
import eu.xenit.alfred.api.versionhistory.VersionHistory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class VersionHistoryWebScriptsTest {

    private static final NodeRef NODE = new NodeRef("workspace://SpacesStore/node");
    private static final String CM = "{http://www.alfresco.org/model/content/1.0}";

    private final IVersionHistoryService versionHistoryService = mock(IVersionHistoryService.class);

    @Test
    void versionHistoryIsWrittenAsJsonAndUnversionedNodeIsEmpty200() throws IOException {
        when(versionHistoryService.GetVersionHistory(NODE))
                .thenReturn(new VersionHistory(List.of(version("1.1"), version("1.0"))));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        new VersionHistoryGetWebScript(versionHistoryService).execute(requestFor(null, null), res);

        assertJsonOk(res);
        JsonNode versions = new ObjectMapper().readTree(out.toString(StandardCharsets.UTF_8)).get("versionHistory");
        assertEquals(2, versions.size());
        assertEquals("1.1", versions.get(0).get("label").asText());

        // The service returns null for an unversioned node.
        when(versionHistoryService.GetVersionHistory(NODE)).thenReturn(null);
        WebScriptResponse emptyRes = mock(WebScriptResponse.class);

        new VersionHistoryGetWebScript(versionHistoryService).execute(requestFor(null, null), emptyRes);

        assertEmptyOk(emptyRes);
    }

    @Test
    void rootAndHeadWriteTheVersion() throws IOException {
        when(versionHistoryService.getRootVersion(NODE)).thenReturn(version("1.0"));
        when(versionHistoryService.getHeadVersion(NODE)).thenReturn(version("2.0"));

        Map<AbstractWebScript, String> expectedLabels = Map.of(
                new RootVersionGetWebScript(versionHistoryService), "1.0",
                new HeadVersionGetWebScript(versionHistoryService), "2.0");
        for (Map.Entry<AbstractWebScript, String> entry : expectedLabels.entrySet()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebScriptResponse res = responseWritingTo(out);

            entry.getKey().execute(requestFor(null, null), res);

            assertJsonOk(res);
            JsonNode version = new ObjectMapper().readTree(out.toString(StandardCharsets.UTF_8));
            assertEquals(entry.getValue(), version.get("label").asText());
        }
    }

    @Test
    void putPassesOnlyTheGivenOptionsAsVersionProperties() throws IOException {
        VersionHistoryPutWebScript webScript = new VersionHistoryPutWebScript(versionHistoryService);
        WebScriptResponse res = mock(WebScriptResponse.class);

        webScript.execute(requestFor(null, "{\"autoVersion\":false,\"initialVersion\":true}"), res);

        verify(versionHistoryService).ensureVersioningEnabled(NODE, Map.of(
                new QName(CM + "autoVersion"), false,
                new QName(CM + "initialVersion"), true));
        assertEmptyOk(res);

        // Without a body, versioning is enabled with Alfresco's defaults.
        webScript.execute(requestFor(null, null), mock(WebScriptResponse.class));

        verify(versionHistoryService).ensureVersioningEnabled(NODE, new HashMap<>());
    }

    @Test
    void deleteHistoryRevertAndDeleteVersionUseTheUrlAndAreEmpty200() throws IOException {
        WebScriptResponse deleteHistoryRes = mock(WebScriptResponse.class);
        new VersionHistoryDeleteWebScript(versionHistoryService).execute(requestFor(null, null), deleteHistoryRes);
        verify(versionHistoryService).deleteVersionHistory(NODE);
        assertEmptyOk(deleteHistoryRes);

        WebScriptResponse revertRes = mock(WebScriptResponse.class);
        new VersionRevertWebScript(versionHistoryService).execute(requestFor("1.2", null), revertRes);
        verify(versionHistoryService).revert(NODE, "1.2");
        assertEmptyOk(revertRes);

        WebScriptResponse deleteVersionRes = mock(WebScriptResponse.class);
        new VersionDeleteWebScript(versionHistoryService).execute(requestFor("1.3", null), deleteVersionRes);
        verify(versionHistoryService).deleteVersion(NODE, "1.3");
        assertEmptyOk(deleteVersionRes);
    }

    private static Version version(String label) {
        return new Version("admin", null, label, "description of " + label, VersionType.MAJOR, NODE);
    }

    private static void assertJsonOk(WebScriptResponse res) {
        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
    }

    /** 200 without a body, like {@code ResponseEntity.ok().build()} in the MVC implementation. */
    private static void assertEmptyOk(WebScriptResponse res) throws IOException {
        verify(res).setStatus(200);
        verify(res).setHeader("Cache-Control", "no-cache");
        verify(res, never()).setContentType(anyString());
        verify(res, never()).getOutputStream();
        verify(res, never()).getWriter();
    }

    /**
     * A request for /apix/v1/versionhistory/{space}/{store}/{guid}/... on {@link #NODE}, with an optional
     * {label} and body.
     */
    private static WebScriptRequest requestFor(String label, String body) throws IOException {
        Map<String, String> vars = new HashMap<>(Map.of(
                "space", NODE.getStoreRefProtocol(), "store", NODE.getStoreRefId(), "guid", NODE.getGuid()));
        if (label != null) {
            vars.put("label", label);
        }
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/versionhistory/{space}/{store}/{guid}", vars,
                "/apix/v1/versionhistory/"));
        if (body != null) {
            Content content = mock(Content.class);
            when(content.getContent()).thenReturn(body);
            when(req.getContent()).thenReturn(content);
        }
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}