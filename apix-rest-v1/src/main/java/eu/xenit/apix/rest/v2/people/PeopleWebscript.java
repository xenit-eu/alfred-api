package eu.xenit.apix.rest.v2.people;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.rest.v2.ApixV2Webscript;
import eu.xenit.apix.rest.v2.RestV2Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Jasperhilven
 */
@WebScript(baseUri = RestV2Config.BaseUrl, families = RestV2Config.Family, defaultFormat = "json",
        description = "Retrieves person information", value = "People")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v2.people.PeopleWebscript")
public class PeopleWebscript extends ApixV2Webscript {

    Logger logger = LoggerFactory.getLogger(PeopleWebscript.class);
    @Autowired
    IPeopleService personService;

    @Uri(value = "/people/id/{space}/{store}/{guid}", method = HttpMethod.GET)
    @ApiOperation(value = "Returns person information", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public void getPerson(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptResponse webScriptResponse) throws IOException {
        logger.debug("Asked person with guid: " + guid);
        try {
            Person p = personService.GetPerson(createNodeRef(space, store, guid));
            writeJsonResponse(webScriptResponse, p);
        } catch (NoSuchElementException noSuchElementException) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
            writeJsonResponse(webScriptResponse, noSuchElementException);
        } catch (IllegalArgumentException illegalArgumentException) {
            webScriptResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            writeJsonResponse(webScriptResponse, illegalArgumentException);
        }
    }

    @Uri(value = "/people", method = HttpMethod.GET)
    @ApiOperation(value = "Returns all people", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person[].class))
    public void getAllPeople(WebScriptResponse webScriptResponse) throws IOException {
        List<Person> people = personService.GetPeople();
        writeJsonResponse(webScriptResponse, people);
        return;
    }

    @Uri(value = "/people/-me-", method = HttpMethod.GET)
    @ApiOperation(value = "Returns current user information", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public void getPersonCurrentUser(WebScriptResponse webScriptResponse) throws IOException {
        getPersonWithName("-me-", webScriptResponse);
    }

    @Uri(value = "/people/{name}", method = HttpMethod.GET)
    @ApiOperation(value = "Returns person information", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public void getPersonWithName(@UriVariable final String name, WebScriptResponse webScriptResponse)
            throws IOException {
        logger.debug("Asked person with name: " + name);
        try{
            Person p = personService.GetPerson(name);
            writeJsonResponse(webScriptResponse, p);
        } catch (NoSuchElementException noSuchElementException) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
            writeJsonResponse(webScriptResponse, noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            webScriptResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            writeJsonResponse(webScriptResponse, illegalArgumentException.getMessage());
        }
    }

    @Uri(value = "/people/containergroups/{name}", method = HttpMethod.GET)
    @ApiOperation(value = "Returns container groups of person", notes = "")
    @ApiResponses(value = @ApiResponse(code = 200, message = "Success", response = String[].class))
    public void getContainerGroupsOf(@UriVariable final String name, WebScriptResponse webScriptResponse)
            throws IOException {
        logger.debug("Asked containergroups for person with name: " + name);
        Set<String> result = personService.GetContainerGroups(name);
        writeJsonResponse(webScriptResponse, new ArrayList<String>(result));
    }
}
