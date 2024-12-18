package eu.xenit.alfred.api.rest.v2.groups;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.groups.Group;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.v2.AlfredApiV2Webscript;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class GroupsWebscript extends AlfredApiV2Webscript {

    private static final Logger logger = LoggerFactory.getLogger(GroupsWebscript.class);
    private final IPeopleService personService;

    public GroupsWebscript(IPeopleService personService) {
        this.personService = personService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/groups")
    public ResponseEntity<List<Group>> GetAllGroups() {
        return writeJsonResponse(personService.GetGroups());
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/groups/{name}/people")
    public ResponseEntity<?> GetPeopleOfGroup(@PathVariable final String name,
            @RequestParam(required = false) Boolean immediate) {
        if (immediate == null) {
            immediate = false;
        }
        List<Person> people = personService.GetUsersOfGroup(name, immediate);
        if (people == null) {
            return giveNoGroup404(name);
        }
        return ResponseEntity.ok(people);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/groups/{name}/groups")
    public ResponseEntity<?> GetGroupsOfGroup(@PathVariable final String name,
            @RequestParam(required = false) Boolean immediate) {
        if (immediate == null) {
            immediate = false;
        }

        List<Group> groups = personService.GetSubgroupsInGroup(name, immediate);
        if (groups == null) {
            return giveNoGroup404(name);
        }
        return writeJsonResponse(groups);
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v2/groups/{name}/people")
    public ResponseEntity<?> SetPeopleInGroup(@PathVariable final String name,
            @RequestBody SetUsersInGroupOptions options) {
        // We want to replace all of the users in group {name} by a new list of users
        // We're going to avoid unlinking and re-linking the same user, because iterating over the list to check for
        // duplicates is going to be cheaper than unnecessarily invoking all of Alfresco's internal safety checking
        List<Person> linkedUsers = personService.GetUsersOfGroup(name, true);

        logger.debug("Setting new list of users for {}", name);
        // error handling, if {name} isn't a group
        if (linkedUsers == null) {
            return giveNoGroup404(name);
        }

        List<String> oldUsers = new ArrayList<>();
        for (Person p : linkedUsers) {
            oldUsers.add(p.getUserName());
        }

        List<String> newUsers = Arrays.asList(options.getUsers());
        replaceAuthorities(name, oldUsers, newUsers);
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v2/groups/{name}/groups")
    public ResponseEntity<?> SetGroupsOfGroup(@PathVariable final String name,
            @RequestBody SetSubgroupOptions options) {
        // We want to replace all of the subgroups of {name} by a new list of subgroups
        // We're going to avoid unlinking and re-linking the same group, because iterating over the list to check for
        // duplicates is going to be cheaper than unnecessarily invoking all of Alfresco's internal safety checking
        List<Group> linkedGroups = personService.GetSubgroupsInGroup(name, true);

        logger.debug("Setting new list of subgroups for {}", name);
        // error handling, if {name} isn't a group
        if (linkedGroups == null) {
            return giveNoGroup404(name);
        }

        List<String> oldGroups = new ArrayList<>();
        for (Group g : linkedGroups) {
            oldGroups.add(g.getIdentifier());
        }

        List<String> newGroups = Arrays.asList(options.getSubgroups());
        replaceAuthorities(name, oldGroups, newGroups);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<String> giveNoGroup404(String name) {
        return ResponseEntity.status(HttpStatus.SC_NOT_FOUND)
                .body("Group " + name + " does not exist");
    }

    private void replaceAuthorities(String parentGroup, List<String> oldOnes, List<String> newOnes) {
        /* oldOnes are the users/groups currently in the parentGroup. newOnes are the ones to replace them. Any
         * user/group that appears in both oldOnes and newOnes will just stay linked to the parentGroup. The others
         * in oldOnes will be unlinked, whereas the others in newOnes will get linked to the parentGroup.
         */

        // Unlink all currently linked authorities (except those that are in the list of new authorities)
        for (String oldie : oldOnes) {
            logger.debug("Linked authority {} found", oldie);
            if (!newOnes.contains(oldie)) {
                logger.debug("Unlinking {} from its parent group...", oldie);
                personService.UnlinkFromParentGroup(parentGroup, oldie);
            }
        }
        // Link all of the new groups (except those that are still linked)
        for (String newbie : newOnes) {
            if (!oldOnes.contains(newbie)) {
                logger.debug("Adding {} as child to {}", newbie, parentGroup);
                personService.AddToParentGroup(parentGroup, newbie);
            }
        }
    }
}
