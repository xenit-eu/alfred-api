package eu.xenit.alfred.api.rest.v1.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class PersonWebScriptsTest {

    private static final String REF = "workspace://SpacesStore/11111111-1111-1111-1111-111111111111";

    private final IPeopleService peopleService = mock(IPeopleService.class);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void missingUserNameIsBadRequest() {
        WebScriptRequest req = mock(WebScriptRequest.class);
        WebScriptException e = assertThrows(WebScriptException.class,
                () -> new PersonByUsernameWebScript(peopleService).execute(req, response()));
        assertEquals(400, e.getStatus());
    }

    @Test
    void personIsWrittenAsJson() throws IOException {
        when(peopleService.GetPerson("admin")).thenReturn(person("admin"));
        WebScriptResponse res = response();

        new PersonByUsernameWebScript(peopleService).execute(userNameRequest("admin"), res);

        verify(res).setStatus(200);
        assertEquals("admin", new ObjectMapper().readTree(out.toByteArray()).get("userName").asText());
    }

    @Test
    void serviceExceptionsBecomePlainTextErrors() throws IOException {
        when(peopleService.GetPerson("")).thenThrow(new IllegalArgumentException("Username cannot be null or empty"));
        when(peopleService.GetPerson("Україна")).thenThrow(new NoSuchElementException("User Україна does not exist"));

        WebScriptResponse badRequest = response();
        new PersonByUsernameWebScript(peopleService).execute(userNameRequest(""), badRequest);
        verify(badRequest).setStatus(400);
        assertEquals("Username cannot be null or empty", body());

        out.reset();
        WebScriptResponse notFound = response();
        new PersonByUsernameWebScript(peopleService).execute(userNameRequest("Україна"), notFound);
        verify(notFound).setStatus(404);
        verify(notFound).setContentType("text/plain;charset=UTF-8");
        assertEquals("User Україна does not exist", body());
    }

    @Test
    void nodeRefIsBuiltFromTheTemplateVariables() throws IOException {
        when(peopleService.GetPerson(new NodeRef(REF))).thenReturn(person("admin"));
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/people/{space}/{store}/{guid}",
                Map.of("space", "workspace", "store", "SpacesStore", "guid", "11111111-1111-1111-1111-111111111111"),
                "/apix/v1/people/"));
        WebScriptResponse res = response();

        new PersonByNodeRefWebScript(peopleService).execute(req, res);

        // Without this, a wrong NodeRef would still pass: the unstubbed mock returns null, written as a 200.
        verify(peopleService).GetPerson(new NodeRef(REF));
        verify(res).setStatus(200);
    }

    private static Person person(String userName) {
        return new Person(new NodeRef(REF), userName, "First", "Last", "mail@example.com", List.of());
    }

    private static WebScriptRequest userNameRequest(String userName) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getParameter("userName")).thenReturn(userName);
        return req;
    }

    private WebScriptResponse response() {
        WebScriptResponse res = mock(WebScriptResponse.class);
        try {
            when(res.getOutputStream()).thenReturn(out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return res;
    }

    private String body() {
        return out.toString(StandardCharsets.UTF_8);
    }
}
