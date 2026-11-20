package eu.xenit.alfred.api.rest.v2.people;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/people/containergroups/{name}: the names of the groups containing a user.
 * <p>
 * Served on the URL documented in Swagger. The MVC mapping had an accidental space
 * ("/v2/ people/containergroups/{name}"), so this URL returned 404 before the migration.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.people.containergroups.get")
public class ContainerGroupsWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public ContainerGroupsWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String name = req.getServiceMatch().getTemplateVars().get("name");
        JsonResponses.writeJson(res, new ArrayList<>(peopleService.GetContainerGroups(name)));
    }
}
