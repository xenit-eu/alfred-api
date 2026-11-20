package eu.xenit.alfred.api.rest.v2.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/people: all people.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.people.people.get")
public class PeopleListWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public PeopleListWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        JsonResponses.writeJson(res, peopleService.GetPeople());
    }
}
