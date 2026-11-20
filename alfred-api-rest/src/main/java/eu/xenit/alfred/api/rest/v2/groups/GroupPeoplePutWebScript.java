package eu.xenit.alfred.api.rest.v2.groups;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.webscript.JsonRequests;
import eu.xenit.alfred.api.rest.webscript.TextResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * PUT /apix/v2/groups/{name}/people: replaces the users in a group with the {@code users} in the body.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.groups.group-people.put")
public class GroupPeoplePutWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public GroupPeoplePutWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // As before the migration, the body is bound (415/400) before the group is looked up (404).
        String name = GroupWebScriptSupport.groupName(req);
        SetUsersInGroupOptions options = JsonRequests.readBody(req, SetUsersInGroupOptions.class);

        // Only direct members are replaced.
        List<Person> linkedUsers = peopleService.GetUsersOfGroup(name, true);
        if (linkedUsers == null) {
            GroupWebScriptSupport.writeNoGroup(res, name);
            return;
        }
        List<String> oldUsers = new ArrayList<>();
        for (Person p : linkedUsers) {
            oldUsers.add(p.getUserName());
        }
        // Deliberately preserved: a body without "users" fails here with a NullPointerException (500).
        List<String> newUsers = Arrays.asList(options.getUsers());
        GroupWebScriptSupport.replaceAuthorities(peopleService, name, oldUsers, newUsers);
        TextResponses.writeEmpty(res);
    }
}
