package eu.xenit.apix.rest.v1.versionhistory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by stan on 5/2/16.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves version history information", value = "VersionHistory")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.versionhistory.VersionHistoryWebScript1")
public class VersionHistoryWebScript1 extends ApixV1Webscript {

    private final QName PROP_VERSION_LABEL = new QName("{http://www.alfresco.org/model/content/1.0}versionLabel");
    private final QName PROP_INITIAL_VERSION = new QName("{http://www.alfresco.org/model/content/1.0}initialVersion");
    private final QName PROP_AUTO_VERSION = new QName("{http://www.alfresco.org/model/content/1.0}autoVersion");
    private final QName PROP_AUTO_VERSION_PROPS = new QName(
            "{http://www.alfresco.org/model/content/1.0}autoVersionOnUpdateProps");
    private final QName PROP_VERSION_TYPE = new QName("{http://www.alfresco.org/model/content/1.0}versionType");

    //@Autowired
    //private ISearchService service;

    Logger logger = LoggerFactory.getLogger(VersionHistoryWebScript1.class);
    @Autowired
    IVersionHistoryService versionHistoryService;

    @Uri(value = "/versionhistory/{space}/{store}/{guid}/versions", method = HttpMethod.GET)
    @ApiOperation(value = "Returns list of chronological version information for give node", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = VersionHistory.class))
    public void getVersionHistory(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptResponse webScriptResponse) throws IOException {
        logger.debug("Asked versionhistory for node with guid: " + guid);
        VersionHistory vH = versionHistoryService.GetVersionHistory(createNodeRef(space, store, guid));
        writeJsonResponse(webScriptResponse, vH);
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}/root", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Version.class))
    @ApiOperation(value = "Returns the root (oldest) version")
    public void getVersionHistoryRoot(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptResponse webScriptResponse) throws IOException {
        Version oldest = versionHistoryService.getRootVersion(createNodeRef(space, store, guid));
        writeJsonResponse(webScriptResponse, oldest);
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}/head", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Version.class))
    @ApiOperation(value = "Returns the head (newest) version")
    public void getVersionHistoryHead(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptResponse webScriptResponse) throws IOException {
        Version newest = versionHistoryService.getHeadVersion(createNodeRef(space, store, guid));
        writeJsonResponse(webScriptResponse, newest);
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    //No method available to disable versioning. deleting will merely reset version history, starting a new history upon a new version change
    @ApiOperation(value = "Permanently emoves version history")
    public void deleteVersionHistory(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptResponse webScriptResponse) throws IOException {
        versionHistoryService.deleteVersionHistory(createNodeRef(space, store, guid));
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}", method = HttpMethod.PUT)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "eu.xenit.apix.rest.v1.versionhistory.VersionOptions", paramType = "body", name = "body")})
    @ApiOperation(value = "Enables versioning for this node, creating an initial version")
    public void setVersionHistory(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse)
            throws IOException {
        HashMap<QName, Serializable> versionProperties = new HashMap<>();

        Content requestContent = webScriptRequest.getContent();
        InputStream requestInputStream = requestContent.getInputStream();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            VersionOptions versionOptions = objectMapper.readValue(requestInputStream, VersionOptions.class);
            if (versionOptions.getAutoVersion() != null) {
                versionProperties.put(PROP_AUTO_VERSION, versionOptions.getAutoVersion());
            }
            if (versionOptions.getAutoVersionOnUpdateProps() != null) {
                versionProperties.put(PROP_AUTO_VERSION_PROPS, versionOptions.getAutoVersionOnUpdateProps());
            }
            if (versionOptions.getInitialVersion() != null) {
                versionProperties.put(PROP_INITIAL_VERSION, versionOptions.getInitialVersion());
            }
        } catch (JsonMappingException ex) {
            boolean isFirstChar = ex.getLocation().getLineNr() == 1 && ex.getLocation().getColumnNr() == 0;
            boolean isAtStart = ex.getLocation().getByteOffset() == 0 || ex.getLocation().getCharOffset() == 0;
            isAtStart |= ex.getLocation().getByteOffset() == -1 && isFirstChar;
            if (!isAtStart) {
                throw ex;
            }
            // Else, this is an exception because there is no body passed to the request
        }

        versionHistoryService.ensureVersioningEnabled(createNodeRef(space, store, guid), versionProperties);
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}/versions/{label}/revert", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiOperation(value = "(Shallow) Revert the node to version with given label")
    public void revertVersionHistory(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, @UriVariable final String label, WebScriptResponse webScriptResponse)
            throws IOException {
        versionHistoryService.revert(createNodeRef(space, store, guid), label);
    }

    @Uri(value = "/versionhistory/{space}/{store}/{guid}/versions/{label}", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiOperation(value = "Permanently remove version with given label")
    public void deleteVersion(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid, @UriVariable final String label, WebScriptResponse webScriptResponse)
            throws IOException {
        versionHistoryService.deleteVersion(createNodeRef(space, store, guid), label);
    }

}
