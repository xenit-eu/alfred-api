package eu.xenit.apix.rest.v1.people;

import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
//        description = "Retrieves person information", value = "People")
//@Authentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.people.PeopleWebscript")
public class PeopleWebscript1 extends ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(PeopleWebscript1.class);
    private final IPeopleService personService;

    public PeopleWebscript1(IPeopleService personService) {
        this.personService = personService;
    }

    @GetMapping(value = "/v1/people/{space}/{store}/{guid}")
    @ApiOperation(value = "Returns person information")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
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

    @GetMapping(value = "/v1/people")
    @ApiOperation(value = "Returns person information given a userName", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public ResponseEntity<?> getPersonViaUserName(@RequestParam final String userName) {
        logger.debug("Asked person with name: {}", userName);
        try{
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
