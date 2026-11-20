package eu.xenit.alfred.api.rest.people;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.rest.webscript.TextResponses;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Person lookup behaviour shared by the v1 and v2 people web scripts.
 */
public final class PersonResponses {

    private PersonResponses() {
    }

    /**
     * Writes the person as JSON. {@link NoSuchElementException} becomes a 404 and {@link IllegalArgumentException} a
     * 400, each with the exception message as a plain-text body.
     */
    public static void writePerson(WebScriptResponse res, Supplier<Person> lookup) throws IOException {
        Person person;
        try {
            person = lookup.get();
        } catch (NoSuchElementException e) {
            TextResponses.writeText(res, Status.STATUS_NOT_FOUND, messageOf(e));
            return;
        } catch (IllegalArgumentException e) {
            TextResponses.writeText(res, Status.STATUS_BAD_REQUEST, messageOf(e));
            return;
        }
        JsonResponses.writeJson(res, person);
    }

    public static NodeRef nodeRef(WebScriptRequest req) {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        return new NodeRef(vars.get("space"), vars.get("store"), vars.get("guid"));
    }

    private static String messageOf(RuntimeException e) {
        // As before the migration, a null message becomes an empty body.
        return e.getMessage() == null ? "" : e.getMessage();
    }
}
