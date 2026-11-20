package eu.xenit.alfred.api.rest.v2.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.people.PersonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/people/{name}: a person by username.
 * <p>
 * Alfresco URI variables can span '/', so {name} could also match /v2/people/-me-, /id/... and /containergroups/...;
 * those templates have more static characters, so Alfresco matches them first.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.people.person.get")
public class PersonByUsernameWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public PersonByUsernameWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String name = req.getServiceMatch().getTemplateVars().get("name");
        PersonResponses.writePerson(res, () -> peopleService.GetPerson(name));
    }
}
