package eu.xenit.apix.rest.v2.groups;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.groups.Group;
import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.rest.v2.ApixV2Webscript;
import eu.xenit.apix.rest.v2.RestV2Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by jasper on 14/03/17.
 */

@WebScript(baseUri = RestV2Config.BaseUrl, families = RestV2Config.Family, defaultFormat = "json",
        description = "Retrieves group information and links users/groups to parent groups", value = "Groups")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v2.groups.GroupsWebscript")
public class GroupsWebscript extends ApixV2Webscript {

    Logger logger = LoggerFactory.getLogger(GroupsWebscript.class);
    @Autowired
    IPeopleService personService;

    @Uri(value = "/groups", method = HttpMethod.GET)
    @ApiOperation(value = "Returns a list containing all groups", notes = "")
    @ApiResponses(@ApiResponse(code = HttpStatus.SC_OK, message = "Success", response = Group[].class))
    public void GetAllGroups(WebScriptResponse webScriptResponse) throws IOException {
        writeJsonResponse(webScriptResponse, personService.GetGroups());
    }

    @Uri(value = "/groups/{name}/people", method = HttpMethod.GET)
    @ApiOperation(value = "Returns the persons within a specific group", notes = "")
    @ApiResponses(@ApiResponse(code = HttpStatus.SC_OK, message = "Success", response = Person[].class))
    public void GetPeopleOfGroup(@UriVariable final String name, @RequestParam(required = false) Boolean immediate,
            WebScriptResponse webScriptResponse) throws IOException {
        if (immediate == null) {
            immediate = false;
        }
        List<Person> people = personService.GetUsersOfGroup(name, immediate);
        if (people == null) {
            giveNoGroup404(webScriptResponse, name);
            return;
        }
        writeJsonResponse(webScriptResponse, people);
    }

    @Uri(value = "/groups/{name}/groups", method = HttpMethod.GET)
    @ApiOperation(value = "Returns the groups within a specific group", notes = "")
    @ApiResponses(@ApiResponse(code = HttpStatus.SC_OK, message = "Success", response = Group[].class))
    public void GetGroupsOfGroup(@UriVariable final String name, @RequestParam(required = false) Boolean immediate,
            WebScriptResponse webScriptResponse) throws IOException {
        if (immediate == null) {
            immediate = false;
        }

        List<Group> groups = personService.GetSubgroupsInGroup(name, immediate);
        if (groups == null) {
            giveNoGroup404(webScriptResponse, name);
            return;
        }
        writeJsonResponse(webScriptResponse, groups);
    }

    @Uri(value = "/groups/{name}/people", method = HttpMethod.PUT)
    @ApiOperation(value = "Sets the complete list of people as direct members of this group", notes = "")
    @ApiResponses(@ApiResponse(code = HttpStatus.SC_OK, message = "Success", response = Group[].class))
    public void SetPeopleInGroup(@UriVariable final String name, SetUsersInGroupOptions options,
            WebScriptResponse webScriptResponse) throws IOException {
        // We want to replace all of the users in group {name} by a new list of users
        // We're going to avoid unlinking and re-linking the same user, because iterating over the list to check for
        // duplicates is going to be cheaper than unnecessarily invoking all of Alfresco's internal safety checking
        List<Person> linkedUsers = personService.GetUsersOfGroup(name, true);

        logger.debug("Setting new list of users for {}", name);
        // error handling, if {name} isn't a group
        if (linkedUsers == null) {
            giveNoGroup404(webScriptResponse, name);
            return;
        }

        List<String> oldUsers = new ArrayList<>();
        for (Person p : linkedUsers) {
            oldUsers.add(p.getUserName());
        }

        List<String> newUsers = Arrays.asList(options.getUsers());
        replaceAuthorities(name, oldUsers, newUsers);
    }


    @Uri(value = "/groups/{name}/groups", method = HttpMethod.PUT)
    @ApiOperation(value = "Sets the complete list of direct subgroups for this group", notes = "")
    @ApiResponses(@ApiResponse(code = HttpStatus.SC_OK, message = "Success", response = Group[].class))
    public void SetGroupsOfGroup(@UriVariable final String name, SetSubgroupOptions options,
            WebScriptResponse webScriptResponse) throws IOException {
        // We want to replace all of the subgroups of {name} by a new list of subgroups
        // We're going to avoid unlinking and re-linking the same group, because iterating over the list to check for
        // duplicates is going to be cheaper than unnecessarily invoking all of Alfresco's internal safety checking
        List<Group> linkedGroups = personService.GetSubgroupsInGroup(name, true);

        logger.debug("Setting new list of subgroups for {}", name);
        // error handling, if {name} isn't a group
        if (linkedGroups == null) {
            giveNoGroup404(webScriptResponse, name);
            return;
        }

        List<String> oldGroups = new ArrayList<>();
        for (Group g : linkedGroups) {
            oldGroups.add(g.getIdentifier());
        }

        List<String> newGroups = Arrays.asList(options.getSubgroups());
        replaceAuthorities(name, oldGroups, newGroups);
    }

    private void giveNoGroup404(WebScriptResponse response, String name) throws IOException {
        response.setStatus(HttpStatus.SC_NOT_FOUND); // 404
        response.getWriter().write("Group " + name + " does not exist");
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
