package eu.xenit.alfred.api.rest.v2.groups;

import eu.xenit.alfred.api.groups.Group;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.List;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * GET /apix/v2/groups/{name}/groups?immediate=...: the subgroups of a group.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v2.groups.group-groups.get")
public class SubgroupsGetWebScript extends AbstractWebScript {

    private final IPeopleService peopleService;

    public SubgroupsGetWebScript(IPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String name = GroupWebScriptSupport.groupName(req);
        List<Group> groups = peopleService.GetSubgroupsInGroup(name, GroupWebScriptSupport.immediate(req));
        if (groups == null) {
            GroupWebScriptSupport.writeNoGroup(res, name);
            return;
        }
        JsonResponses.writeJson(res, groups);
    }
}
