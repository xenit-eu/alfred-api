package eu.xenit.alfred.api.rest.v2.groups;

import eu.xenit.alfred.api.groups.Group;
import eu.xenit.alfred.api.people.IPeopleService;
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
 * PUT /apix/v2/groups/{name}/groups: replaces the subgroups of a group with the {@code subgroups} in the body.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.groups.group-groups.put")
public class SubgroupsPutWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public SubgroupsPutWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // As before the migration, the body is bound (415/400) before the group is looked up (404).
        String name = GroupWebScriptSupport.groupName(req);
        SetSubgroupOptions options = JsonRequests.readBody(req, SetSubgroupOptions.class);

        // Only direct subgroups are replaced.
        List<Group> linkedGroups = peopleService.GetSubgroupsInGroup(name, true);
        if (linkedGroups == null) {
            GroupWebScriptSupport.writeNoGroup(res, name);
            return;
        }
        List<String> oldGroups = new ArrayList<>();
        for (Group g : linkedGroups) {
            oldGroups.add(g.getIdentifier());
        }
        // Deliberately preserved: a body without "subgroups" fails here with a NullPointerException (500).
        List<String> newGroups = Arrays.asList(options.getSubgroups());
        GroupWebScriptSupport.replaceAuthorities(peopleService, name, oldGroups, newGroups);
        TextResponses.writeEmpty(res);
    }
}
