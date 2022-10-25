package eu.xenit.apix.rest.v2.people;

import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.rest.v2.ApixV2Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

//@WebScript(baseUri = RestV2Config.BaseUrl, families = RestV2Config.Family, defaultFormat = "json",
//        description = "Retrieves person information", value = "People")
//@Authentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v2.people.PeopleWebscript")
public class PeopleWebscript extends ApixV2Webscript {

    private static final Logger logger = LoggerFactory.getLogger(PeopleWebscript.class);
    private final IPeopleService personService;

    public PeopleWebscript(IPeopleService personService) {
        this.personService = personService;
    }

    @GetMapping(value = "/v2/people/id/{space}/{store}/{guid}")
    @ApiOperation(value = "Returns person information")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
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

    @GetMapping(value = "/v2/people")
    @ApiOperation(value = "Returns all people")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person[].class))
    public ResponseEntity<List<Person>> getAllPeople() {
        return writeJsonResponse(personService.GetPeople());
    }

    @GetMapping(value = "/v2/people/-me-")
    @ApiOperation(value = "Returns current user information")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public ResponseEntity<?> getPersonCurrentUser() {
        return getPersonWithName("-me-");
    }

    @GetMapping(value = "/v2/people/{name}")
    @ApiOperation(value = "Returns person information")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public ResponseEntity<?> getPersonWithName(@PathVariable final String name) {
        logger.debug("Asked person with name: {}", name);
        try{
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

    @GetMapping(value = "/v2/ people/containergroups/{name}")
    @ApiOperation(value = "Returns container groups of person")
    @ApiResponses(value = @ApiResponse(code = 200, message = "Success", response = String[].class))
    public ResponseEntity<ArrayList<String>> getContainerGroupsOf(@PathVariable final String name) {
        logger.debug("Asked containergroups for person with name: {}", name);
        Set<String> result = personService.GetContainerGroups(name);
        return writeJsonResponse(new ArrayList<>(result));
    }
}
