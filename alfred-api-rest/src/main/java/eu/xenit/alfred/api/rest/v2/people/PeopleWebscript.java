package eu.xenit.alfred.api.rest.v2.people;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.v2.AlfredApiV2Webscript;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class PeopleWebscript extends AlfredApiV2Webscript {

    private static final Logger logger = LoggerFactory.getLogger(PeopleWebscript.class);
    private final IPeopleService personService;

    public PeopleWebscript(IPeopleService personService) {
        this.personService = personService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/people/id/{space}/{store}/{guid}")
    public ResponseEntity<?> getPerson(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        logger.debug("Asked person with guid: {}", guid);
        try {
            return writeJsonResponse(
                    personService.GetPerson(createNodeRef(space, store, guid))
            );
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(illegalArgumentException.getMessage());
        }
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/people")
    public ResponseEntity<List<Person>> getAllPeople() {
        return writeJsonResponse(personService.GetPeople());
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/people/-me-")
    public ResponseEntity<?> getPersonCurrentUser() {
        return getPersonWithName("-me-");
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/people/{name}")
    public ResponseEntity<?> getPersonWithName(@PathVariable final String name) {
        logger.debug("Asked person with name: {}", name);
        try {
            return writeJsonResponse(
                    personService.GetPerson(name)
            );
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(illegalArgumentException.getMessage());
        }
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v2/ people/containergroups/{name}")
    public ResponseEntity<ArrayList<String>> getContainerGroupsOf(@PathVariable final String name) {
        logger.debug("Asked containergroups for person with name: {}", name);
        Set<String> result = personService.GetContainerGroups(name);
        return writeJsonResponse(new ArrayList<>(result));
    }
}
