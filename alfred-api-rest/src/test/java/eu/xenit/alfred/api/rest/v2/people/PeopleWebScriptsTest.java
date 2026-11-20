package eu.xenit.alfred.api.rest.v2.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class PeopleWebScriptsTest {

    private final IPeopleService peopleService = mock(IPeopleService.class);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void meIsTheNameLookupWithMe() throws IOException {
        // The people service resolves "-me-" to the current user, so the web script passes it through unchanged.
        when(peopleService.GetPerson("-me-")).thenReturn(person("admin"));
        WebScriptResponse res = response();

        new CurrentPersonWebScript(peopleService).execute(mock(WebScriptRequest.class), res);

        verify(res).setStatus(200);
        verify(peopleService).GetPerson("-me-");
    }

    @Test
    void dottedNamesArePassedWhole() throws IOException {
        when(peopleService.GetPerson("john.doe")).thenReturn(person("john.doe"));
        WebScriptResponse res = response();

        new PersonByUsernameWebScript(peopleService).execute(nameRequest("john.doe"), res);

        verify(res).setStatus(200);
        verify(peopleService).GetPerson("john.doe");
    }

    @Test
    void containerGroupsAreWrittenAsAJsonArray() throws IOException {
        when(peopleService.GetContainerGroups("admin")).thenReturn(Set.of("GROUP_ALFRESCO_ADMINISTRATORS"));

        new ContainerGroupsWebScript(peopleService).execute(nameRequest("admin"), response());

        assertEquals("[\"GROUP_ALFRESCO_ADMINISTRATORS\"]", out.toString(StandardCharsets.UTF_8));
    }

    private static Person person(String userName) {
        return new Person(new NodeRef("workspace://SpacesStore/1"), userName, "First", "Last", "mail@example.com",
                List.of());
    }

    private static WebScriptRequest nameRequest(String name) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v2/people/{name}", Map.of("name", name),
                "/apix/v2/people/"));
        return req;
    }

    private WebScriptResponse response() throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}
