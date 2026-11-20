package eu.xenit.alfred.api.rest.v0.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.MetadataChanges;
import eu.xenit.alfred.api.node.NodeMetadata;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.permissions.PermissionValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class MetadataWebScriptsTest {

    private static final String NODE = "workspace://SpacesStore/11111111-1111-1111-1111-111111111111";
    private static final String OTHER = "workspace://SpacesStore/22222222-2222-2222-2222-222222222222";

    private final INodeService nodeService = mock(INodeService.class);
    private final IPermissionService permissionService = mock(IPermissionService.class);

    // --- nodeRef parameter: a missing parameter is a 400, anything present goes to the service unchanged.

    @Test
    void missingNodeRefParameterIsBadRequest() {
        WebScriptRequest req = mock(WebScriptRequest.class);
        WebScriptException e = assertThrows(WebScriptException.class, () -> MetadataRequests.nodeRef(req));
        assertEquals(400, e.getStatus());
    }

    @Test
    void presentNodeRefParameterIsPassedOnUnvalidated() {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("nodeRef")).thenReturn("");
        assertEquals("", MetadataRequests.nodeRef(req).getValue());
    }

    // --- the three web scripts (request-body rules are covered by JsonRequestsTest).

    @Test
    void getReturnsV0MetadataForTheNodeRefParameter() throws IOException {
        givenNode(NODE);
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("nodeRef")).thenReturn(NODE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new NodeMetadataGetWebScript(nodeService, permissionService).execute(req, responseWritingTo(out));

        JsonNode json = new ObjectMapper().readTree(out.toByteArray());
        assertEquals(NODE, json.get("id").asText());
        assertEquals("{http://www.alfresco.org/model/content/1.0}content", json.get("type").asText());
        assertTrue(json.get("canEditMetadata").asBoolean());
    }

    @Test
    void postChecksTheNodeRefBeforeTheBody() {
        // The MVC method bound @RequestParam before @RequestBody, so a missing nodeRef wins over a bad content type.
        WebScriptRequest req = postRequest("text/plain", "{}");

        WebScriptException e = assertThrows(WebScriptException.class,
                () -> new NodeMetadataPostWebScript(nodeService, permissionService).execute(req,
                        mock(WebScriptResponse.class)));

        assertEquals(400, e.getStatus());
        verify(nodeService, never()).setMetadata(any(), any());
    }

    @Test
    void postAppliesTheConvertedChanges() throws IOException {
        givenNode(NODE);
        when(nodeService.setMetadata(any(), any())).thenReturn(metadata(NODE));
        WebScriptRequest req = postRequest("application/json", "{\"propertiesToSet\":{\"cm:title\":[\"A title\"]}}");
        when(req.getParameter("nodeRef")).thenReturn(NODE);

        new NodeMetadataPostWebScript(nodeService, permissionService)
                .execute(req, responseWritingTo(new ByteArrayOutputStream()));

        ArgumentCaptor<MetadataChanges> changes = ArgumentCaptor.forClass(MetadataChanges.class);
        verify(nodeService).setMetadata(org.mockito.ArgumentMatchers.eq(new NodeRef(NODE)), changes.capture());
        assertEquals("A title", changes.getValue().getPropertiesToSet().get(new QName("cm:title"))[0]);
    }

    @Test
    void bulkReturnsMetadataInRequestOrderAndAcceptsASingleValue() throws IOException {
        givenNode(NODE);
        givenNode(OTHER);
        NodeMetadataBulkWebScript webScript = new NodeMetadataBulkWebScript(nodeService, permissionService);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        webScript.execute(postRequest("application/json", "[\"" + OTHER + "\",\"" + NODE + "\"]"),
                responseWritingTo(out));
        JsonNode list = new ObjectMapper().readTree(out.toByteArray());
        assertEquals(OTHER, list.get(0).get("id").asText());
        assertEquals(NODE, list.get(1).get("id").asText());

        // ACCEPT_SINGLE_VALUE_AS_ARRAY in the shared Jackson configuration: a lone string is a one-element list.
        ByteArrayOutputStream single = new ByteArrayOutputStream();
        webScript.execute(postRequest("application/json", "\"" + NODE + "\""), responseWritingTo(single));
        assertEquals(1, new ObjectMapper().readTree(single.toByteArray()).size());
    }

    private void givenNode(String nodeRef) {
        when(nodeService.getMetadata(new NodeRef(nodeRef))).thenReturn(metadata(nodeRef));
        when(permissionService.getPermissions(new NodeRef(nodeRef)))
                .thenReturn(Map.of(IPermissionService.WRITE, PermissionValue.ALLOW));
    }

    private static NodeMetadata metadata(String nodeRef) {
        QName content = new QName("{http://www.alfresco.org/model/content/1.0}content");
        return new NodeMetadata(new NodeRef(nodeRef), content, content, 1L,
                Map.of(new QName("{http://www.alfresco.org/model/content/1.0}name"), List.of("name")), List.of());
    }

    private static WebScriptRequest postRequest(String contentType, String body) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        Content content = mock(Content.class);
        try {
            when(content.getInputStream()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        when(req.getContentType()).thenReturn(contentType);
        when(req.getContent()).thenReturn(content);
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}
