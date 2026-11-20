package eu.xenit.alfred.api.rest.v2.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.groups.Group;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class GroupWebScriptsTest {

    private static final String GROUP = "GROUP_test";

    private final IPeopleService peopleService = mock(IPeopleService.class);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void immediateFollowsSpringBooleanConversion() {
        assertFalse(GroupWebScriptSupport.immediate(immediateRequest(null)));
        assertFalse(GroupWebScriptSupport.immediate(immediateRequest("")));
        assertTrue(GroupWebScriptSupport.immediate(immediateRequest("true")));
        assertTrue(GroupWebScriptSupport.immediate(immediateRequest("yes")));
        assertFalse(GroupWebScriptSupport.immediate(immediateRequest("false")));
        WebScriptException e = assertThrows(WebScriptException.class,
                () -> GroupWebScriptSupport.immediate(immediateRequest("abc")));
        assertEquals(400, e.getStatus());
    }

    @Test
    void unknownGroupIsAPlainText404() throws IOException {
        // The people service returns null for an unknown group (an unstubbed mock would return an empty list).
        when(peopleService.GetUsersOfGroup(GROUP, false)).thenReturn(null);
        WebScriptRequest req = groupRequest(null, null);
        WebScriptResponse res = response();

        new GroupPeopleGetWebScript(peopleService).execute(req, res);

        verify(res).setStatus(404);
        assertEquals("Group " + GROUP + " does not exist", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void putReplacesOnlyTheChangedMembers() throws IOException {
        when(peopleService.GetUsersOfGroup(GROUP, true)).thenReturn(List.of(person("kept"), person("removed")));
        WebScriptResponse res = response();

        new GroupPeoplePutWebScript(peopleService)
                .execute(groupRequest("application/json", "{\"users\":[\"kept\",\"added\"]}"), res);

        verify(peopleService).UnlinkFromParentGroup(GROUP, "removed");
        verify(peopleService).AddToParentGroup(GROUP, "added");
        verify(peopleService, never()).UnlinkFromParentGroup(GROUP, "kept");
        verify(peopleService, never()).AddToParentGroup(GROUP, "kept");
        verify(res).setStatus(200);
    }

    @Test
    void putSubgroupsComparesGroupIdentifiers() throws IOException {
        when(peopleService.GetSubgroupsInGroup(GROUP, true)).thenReturn(List.of(group("GROUP_old")));

        new SubgroupsPutWebScript(peopleService)
                .execute(groupRequest("application/json", "{\"subgroups\":[\"GROUP_new\"]}"), response());

        verify(peopleService).UnlinkFromParentGroup(GROUP, "GROUP_old");
        verify(peopleService).AddToParentGroup(GROUP, "GROUP_new");
    }

    @Test
    void putBindsTheBodyBeforeLookingUpTheGroup() {
        WebScriptException e = assertThrows(WebScriptException.class, () -> new GroupPeoplePutWebScript(peopleService)
                .execute(groupRequest("text/plain", "{\"users\":[]}"), response()));

        assertEquals(415, e.getStatus());
        verifyNoInteractions(peopleService);
    }

    @Test
    void putWithoutTheRequiredFieldKeepsTheExistingBehaviour() throws IOException {
        // Unknown group: 404 before the missing field is noticed.
        when(peopleService.GetUsersOfGroup(GROUP, true)).thenReturn(null);
        WebScriptResponse res = response();
        new GroupPeoplePutWebScript(peopleService).execute(groupRequest("application/json", "{}"), res);
        verify(res).setStatus(404);

        // Existing group: the missing field fails with a NullPointerException (a 500), and nothing is changed.
        when(peopleService.GetUsersOfGroup(GROUP, true)).thenReturn(List.of());
        assertThrows(NullPointerException.class, () -> new GroupPeoplePutWebScript(peopleService)
                .execute(groupRequest("application/json", "{}"), response()));
        verify(peopleService, never()).AddToParentGroup(anyString(), anyString());
        verify(peopleService, never()).UnlinkFromParentGroup(anyString(), anyString());
    }

    @Test
    void getPassesImmediateToTheService() throws IOException {
        when(peopleService.GetSubgroupsInGroup(anyString(), anyBoolean())).thenReturn(List.of());
        WebScriptRequest req = groupRequest(null, null);
        when(req.getParameter("immediate")).thenReturn("yes");

        new SubgroupsGetWebScript(peopleService).execute(req, response());

        verify(peopleService).GetSubgroupsInGroup(GROUP, true);
    }

    private static Person person(String userName) {
        return new Person(new NodeRef("workspace://SpacesStore/1"), userName, "First", "Last", "mail@example.com",
                List.of());
    }

    private static Group group(String identifier) {
        return new Group(new NodeRef("workspace://SpacesStore/" + identifier), identifier, identifier);
    }

    private static WebScriptRequest immediateRequest(String value) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("immediate")).thenReturn(value);
        return req;
    }

    private static WebScriptRequest groupRequest(String contentType, String body) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v2/groups/{name}/people", Map.of("name", GROUP),
                "/apix/v2/groups/"));
        if (body != null) {
            Content content = mock(Content.class);
            try {
                when(content.getInputStream())
                        .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            when(req.getContentType()).thenReturn(contentType);
            when(req.getContent()).thenReturn(content);
        }
        return req;
    }

    private WebScriptResponse response() throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}
