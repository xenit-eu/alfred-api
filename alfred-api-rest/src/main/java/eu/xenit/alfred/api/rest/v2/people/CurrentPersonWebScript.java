package eu.xenit.alfred.api.rest.v2.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.people.PersonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/people/-me-: the current user, looked up by the name "-me-", which the people service resolves to the
 * current user.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.people.me.get")
public class CurrentPersonWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public CurrentPersonWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        PersonResponses.writePerson(res, () -> peopleService.GetPerson("-me-"));
    }
}
