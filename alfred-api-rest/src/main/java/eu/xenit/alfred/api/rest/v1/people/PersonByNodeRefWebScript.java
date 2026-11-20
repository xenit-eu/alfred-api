package eu.xenit.alfred.api.rest.v1.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.people.PersonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v1/people/{space}/{store}/{guid}: a person by the node reference of their person node.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v1.people.person.get")
public class PersonByNodeRefWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public PersonByNodeRefWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        PersonResponses.writePerson(res, () -> peopleService.GetPerson(PersonResponses.nodeRef(req)));
    }
}
