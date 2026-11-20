package eu.xenit.alfred.api.rest.v2.groups;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.List;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/groups/{name}/people?immediate=...: the users in a group.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.groups.group-people.get")
public class GroupPeopleGetWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public GroupPeopleGetWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String name = GroupWebScriptSupport.groupName(req);
        List<Person> people = peopleService.GetUsersOfGroup(name, GroupWebScriptSupport.immediate(req));
        if (people == null) {
            GroupWebScriptSupport.writeNoGroup(res, name);
            return;
        }
        JsonResponses.writeJson(res, people);
    }
}
