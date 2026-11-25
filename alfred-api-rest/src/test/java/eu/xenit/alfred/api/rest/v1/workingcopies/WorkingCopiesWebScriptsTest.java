package eu.xenit.alfred.api.rest.v1.workingcopies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class WorkingCopiesWebScriptsTest {

    private static final NodeRef ORIGINAL = new NodeRef("workspace://SpacesStore/original");
    private static final NodeRef WORKING_COPY = new NodeRef("workspace://SpacesStore/working-copy");
    private static final NodeRef DESTINATION = new NodeRef("workspace://SpacesStore/destination");

    private final INodeService nodeService = mock(INodeService.class);

    @Test
    void checkoutToDestinationFolderWritesTheWorkingCopy() throws IOException {
        when(nodeService.exists(ORIGINAL)).thenReturn(true);
        when(nodeService.exists(DESTINATION)).thenReturn(true);
        when(nodeService.checkout(ORIGINAL, DESTINATION)).thenReturn(WORKING_COPY);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        execute(new WorkingCopyCreateWebScript(nodeService), requestWithBody(
                "{\"original\":\"" + ORIGINAL + "\",\"destinationFolder\":\"" + DESTINATION + "\"}"), res);

        assertJsonOk(res);
        assertEquals("{\"noderef\":\"" + WORKING_COPY + "\"}", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void checkoutOfMissingOriginalOrDestinationFolderIs404() throws IOException {
        WorkingCopyCreateWebScript webScript = new WorkingCopyCreateWebScript(nodeService);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);
        execute(webScript, requestWithBody("{\"original\":\"" + ORIGINAL + "\"}"), res);
        assertDoesNotExist(res, out, ORIGINAL);

        when(nodeService.exists(ORIGINAL)).thenReturn(true);
        out = new ByteArrayOutputStream();
        res = responseWritingTo(out);
        execute(webScript, requestWithBody(
                "{\"original\":\"" + ORIGINAL + "\",\"destinationFolder\":\"" + DESTINATION + "\"}"), res);
        assertDoesNotExist(res, out, DESTINATION);

        verify(nodeService, never()).checkout(any(), any());
    }

    @Test
    void missingOrInvalidBodyIs400() {
        when(nodeService.exists(WORKING_COPY)).thenReturn(true);

        for (String body : new String[]{null, "  ", "not json"}) {
            assertBadRequest(new WorkingCopyCreateWebScript(nodeService), requestWithBody(body), body);
        }
        assertBadRequest(new WorkingCopyCheckinWebScript(nodeService), requestFor(WORKING_COPY, null), null);

        verify(nodeService, never()).checkout(any(), any());
        verify(nodeService, never()).checkin(any(), any(), anyBoolean());
    }

    @Test
    void checkinPassesCommentAndVersionTypeAndWritesTheOriginal() throws IOException {
        when(nodeService.exists(WORKING_COPY)).thenReturn(true);
        when(nodeService.checkin(WORKING_COPY, "my comment", true)).thenReturn(ORIGINAL);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        execute(new WorkingCopyCheckinWebScript(nodeService),
                requestFor(WORKING_COPY, "{\"comment\":\"my comment\",\"majorVersion\":true}"), res);

        assertJsonOk(res);
        assertEquals("{\"noderef\":\"" + ORIGINAL + "\"}", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void cancelAndOriginalWriteTheOriginal() throws IOException {
        when(nodeService.exists(WORKING_COPY)).thenReturn(true);
        when(nodeService.cancelCheckout(WORKING_COPY)).thenReturn(ORIGINAL);
        when(nodeService.getWorkingCopySource(WORKING_COPY)).thenReturn(ORIGINAL);

        for (AbstractWebScript webScript : List.of(new WorkingCopyCancelWebScript(nodeService),
                new WorkingCopyOriginalWebScript(nodeService))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebScriptResponse res = responseWritingTo(out);

            execute(webScript, requestFor(WORKING_COPY, null), res);

            assertJsonOk(res);
            assertEquals("{\"noderef\":\"" + ORIGINAL + "\"}", out.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void missingWorkingCopyIs404() throws IOException {
        for (AbstractWebScript webScript : List.of(new WorkingCopyCheckinWebScript(nodeService),
                new WorkingCopyCancelWebScript(nodeService), new WorkingCopyOriginalWebScript(nodeService))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebScriptResponse res = responseWritingTo(out);

            execute(webScript, requestFor(WORKING_COPY, "{\"comment\":\"\",\"majorVersion\":false}"), res);

            assertDoesNotExist(res, out, WORKING_COPY);
        }
        verify(nodeService, never()).checkin(any(), any(), anyBoolean());
        verify(nodeService, never()).cancelCheckout(any());
        verify(nodeService, never()).getWorkingCopySource(any());
    }

    private static void assertJsonOk(WebScriptResponse res) {
        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
    }

    private static void assertDoesNotExist(WebScriptResponse res, ByteArrayOutputStream out, NodeRef nodeRef) {
        verify(res).setStatus(404);
        verify(res).setContentType("text/plain;charset=UTF-8");
        verify(res).setHeader("Cache-Control", "no-cache");
        assertEquals(nodeRef + " does not exist.", out.toString(StandardCharsets.UTF_8));
    }

    private static void assertBadRequest(AbstractWebScript webScript, WebScriptRequest req, String body) {
        WebScriptResponse res = mock(WebScriptResponse.class);
        WebScriptException e = assertThrows(WebScriptException.class, () -> execute(webScript, req, res),
                "body: " + body);
        assertEquals(400, e.getStatus(), "body: " + body);
        verifyNoInteractions(res);
    }

    private static void execute(AbstractWebScript webScript, WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        webScript.execute(req, res);
    }

    /** A request for /apix/v1/workingcopies/{space}/{store}/{guid}/..., with an optional body. */
    private static WebScriptRequest requestFor(NodeRef nodeRef, String body) {
        WebScriptRequest req = requestWithBody(body);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/workingcopies/{space}/{store}/{guid}",
                Map.of("space", nodeRef.getStoreRefProtocol(), "store", nodeRef.getStoreRefId(),
                        "guid", nodeRef.getGuid()),
                "/apix/v1/workingcopies/"));
        return req;
    }

    /** A JSON request whose body is {@code body}; {@code null} means the request has no body. */
    private static WebScriptRequest requestWithBody(String body) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getContentType()).thenReturn("application/json");
        if (body != null) {
            Content content = mock(Content.class);
            when(content.getInputStream()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
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