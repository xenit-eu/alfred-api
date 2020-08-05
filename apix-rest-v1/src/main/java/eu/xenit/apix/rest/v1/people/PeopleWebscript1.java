package eu.xenit.apix.rest.v1.people;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.people.IPeopleService;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Jasperhilven on 24-Oct-16.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves person information", value = "People")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.people.PeopleWebscript")
public class PeopleWebscript1 extends ApixV1Webscript {

    Logger logger = LoggerFactory.getLogger(eu.xenit.apix.rest.v1.people.PeopleWebscript1.class);
    @Autowired
    IPeopleService personService;

    @Uri(value = "/people/{space}/{store}/{guid}", method = HttpMethod.GET)
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
            writeJsonResponse(webScriptResponse, noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            webScriptResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            writeJsonResponse(webScriptResponse, illegalArgumentException.getMessage());
        }
    }

    @Uri(value = "/people", method = HttpMethod.GET)
    @ApiOperation(value = "Returns person information given a userName", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Person.class))
    public void getPersonViaUserName(@RequestParam final String userName, WebScriptResponse webScriptResponse)
            throws IOException {
        logger.debug("Asked person with name: " + userName);
        try{
            Person p = personService.GetPerson(userName);
            writeJsonResponse(webScriptResponse, p);
        } catch (NoSuchElementException noSuchElementException) {
            webScriptResponse.setStatus(HttpStatus.SC_NOT_FOUND);
            writeJsonResponse(webScriptResponse, noSuchElementException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            webScriptResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            writeJsonResponse(webScriptResponse, illegalArgumentException.getMessage());
        }
    }
}
