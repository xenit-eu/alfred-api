package eu.xenit.alfred.api.rest.v1.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.people.PersonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v1/people?userName=...: a person by username ("-me-" is the current user).
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.people.people.get")
public class PersonByUsernameWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public PersonByUsernameWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // Missing is a 400 here; an empty value is passed on and rejected by the service (also a 400).
        String userName = req.getParameter("userName");
        if (userName == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required parameter 'userName' is not present.");
        }
        PersonResponses.writePerson(res, () -> peopleService.GetPerson(userName));
    }
}
