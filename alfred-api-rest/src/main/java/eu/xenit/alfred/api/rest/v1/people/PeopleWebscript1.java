package eu.xenit.alfred.api.rest.v1.people;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class PeopleWebscript1 extends AlfredApiV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(PeopleWebscript1.class);
    private final IPeopleService personService;

    public PeopleWebscript1(IPeopleService personService) {
        this.personService = personService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/people/{space}/{store}/{guid}")
    public ResponseEntity<?> getPerson(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        logger.debug("Asked person with guid: {}", guid);
        try {
            return writeJsonResponse(
                    personService.GetPerson(
                            createNodeRef(space, store, guid)
                    )
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
    @GetMapping(value = "/v1/people")
    public ResponseEntity<?> getPersonViaUserName(@RequestParam final String userName) {
        logger.debug("Asked person with name: {}", userName);
        try {
            return writeJsonResponse(
                    personService.GetPerson(userName)
            );
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(illegalArgumentException.getMessage());
        }
    }
}
