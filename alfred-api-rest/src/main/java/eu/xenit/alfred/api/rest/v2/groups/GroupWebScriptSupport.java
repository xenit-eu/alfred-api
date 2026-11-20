package eu.xenit.alfred.api.rest.v2.groups;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.webscript.TextResponses;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Request parsing, the "no such group" response and membership replacement for the v2 group web scripts.
 */
final class GroupWebScriptSupport {

    private static final Logger logger = LoggerFactory.getLogger(GroupWebScriptSupport.class);

    private GroupWebScriptSupport() {
    }

    /** The group identifier, including its GROUP_ prefix. */
    static String groupName(WebScriptRequest req) {
        return req.getServiceMatch().getTemplateVars().get("name");
    }

    /**
     * Converts the optional immediate parameter using Spring's Boolean conversion rules.
     * Missing or empty values default to false; invalid values result in a 400 response.
     */
    static boolean immediate(WebScriptRequest req) {
        String value = req.getParameter("immediate");
        Boolean immediate;
        try {
            immediate = value == null ? null : DefaultConversionService.getSharedInstance().convert(value, Boolean.class);
        } catch (ConversionException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Invalid value for parameter 'immediate': " + value);
        }
        return Boolean.TRUE.equals(immediate);
    }

    static void writeNoGroup(WebScriptResponse res, String name) throws IOException {
        TextResponses.writeText(res, Status.STATUS_NOT_FOUND, "Group " + name + " does not exist");
    }

    /**
     * Makes {@code newOnes} the direct members (users or groups) of {@code parentGroup}, given its current members
     * {@code oldOnes}. Only the differences are unlinked or linked.
     */
    static void replaceAuthorities(IPeopleService peopleService, String parentGroup, List<String> oldOnes,
            List<String> newOnes) {
        // Unlink current members that are not in the new list.
        for (String oldie : oldOnes) {
            logger.debug("Linked authority {} found", oldie);
            if (!newOnes.contains(oldie)) {
                logger.debug("Unlinking {} from its parent group...", oldie);
                peopleService.UnlinkFromParentGroup(parentGroup, oldie);
            }
        }
        // Link new members that are not linked yet.
        for (String newbie : newOnes) {
            if (!oldOnes.contains(newbie)) {
                logger.debug("Adding {} as child to {}", newbie, parentGroup);
                peopleService.AddToParentGroup(parentGroup, newbie);
            }
        }
    }
}
