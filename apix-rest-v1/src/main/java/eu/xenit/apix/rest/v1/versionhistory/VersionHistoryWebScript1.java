package eu.xenit.apix.rest.v1.versionhistory;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.Serializable;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by stan on 5/2/16.
 */
//@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
//        description = "Retrieves version history information", value = "VersionHistory")
//@Authentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.versionhistory.VersionHistoryWebScript1")
public class VersionHistoryWebScript1 extends ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(VersionHistoryWebScript1.class);

    private static final QName PROP_INITIAL_VERSION =
            new QName("{http://www.alfresco.org/model/content/1.0}initialVersion");
    private static final QName PROP_AUTO_VERSION =
            new QName("{http://www.alfresco.org/model/content/1.0}autoVersion");
    private static final QName PROP_AUTO_VERSION_PROPS =
            new QName(
            "{http://www.alfresco.org/model/content/1.0}autoVersionOnUpdateProps");
    private final IVersionHistoryService versionHistoryService;

    public VersionHistoryWebScript1(IVersionHistoryService versionHistoryService) {
        this.versionHistoryService = versionHistoryService;
    }

    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions")
    @ApiOperation(value = "Returns list of chronological version information for give node")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = VersionHistory.class))
    public ResponseEntity<VersionHistory> getVersionHistory(@PathVariable final String space,
                                                            @PathVariable final String store,
                                                            @PathVariable final String guid) {
        logger.debug("Asked versionhistory for node with guid: {}", guid);
        return writeJsonResponse(
                versionHistoryService.GetVersionHistory(createNodeRef(space, store, guid))
        );
    }

    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/root")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Version.class))
    @ApiOperation(value = "Returns the root (oldest) version")
    public ResponseEntity<Version> getVersionHistoryRoot(@PathVariable final String space,
                                                         @PathVariable final String store,
                                                         @PathVariable final String guid) {
        return writeJsonResponse(
                versionHistoryService.getRootVersion(createNodeRef(space, store, guid))
        );
    }

    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/head")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Version.class))
    @ApiOperation(value = "Returns the head (newest) version")
    public ResponseEntity<Version> getVersionHistoryHead(@PathVariable final String space,
                                                         @PathVariable final String store,
                                                         @PathVariable final String guid) {
        return writeJsonResponse(
                versionHistoryService.getHeadVersion(createNodeRef(space, store, guid))
        );
    }

    @DeleteMapping(value = "/v1/versionhistory/{space}/{store}/{guid}")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    //No method available to disable versioning. deleting will merely reset version history,
    // starting a new history upon a new version change
    @ApiOperation(value = "Permanently emoves version history")
    public ResponseEntity<?> deleteVersionHistory(@PathVariable final String space,
                                                       @PathVariable final String store,
                                                       @PathVariable final String guid) {
        versionHistoryService.deleteVersionHistory(createNodeRef(space, store, guid));
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/v1/versionhistory/{space}/{store}/{guid}")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "eu.xenit.apix.rest.v1.versionhistory.VersionOptions",
                    paramType = "body", name = "body")})
    @ApiOperation(value = "Enables versioning for this node, creating an initial version")
    public ResponseEntity<?> setVersionHistory(@PathVariable final String space,
                                                @PathVariable final String store,
                                                @PathVariable final String guid,
                                                @RequestBody(required = false) final VersionOptions versionOptions) {
        HashMap<QName, Serializable> versionProperties = new HashMap<>();
        if(versionOptions != null) {
            if (versionOptions.getAutoVersion() != null) {
                versionProperties.put(PROP_AUTO_VERSION, versionOptions.getAutoVersion());
            }
            if (versionOptions.getAutoVersionOnUpdateProps() != null) {
                versionProperties.put(PROP_AUTO_VERSION_PROPS, versionOptions.getAutoVersionOnUpdateProps());
            }
            if (versionOptions.getInitialVersion() != null) {
                versionProperties.put(PROP_INITIAL_VERSION, versionOptions.getInitialVersion());
            }
        }
        versionHistoryService.ensureVersioningEnabled(createNodeRef(space, store, guid), versionProperties);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions/{label}/revert")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiOperation(value = "(Shallow) Revert the node to version with given label")
    public ResponseEntity<?> revertVersionHistory(@PathVariable final String space,
                                                       @PathVariable final String store,
                                                       @PathVariable final String guid,
                                                       @PathVariable final String label) {
        versionHistoryService.revert(createNodeRef(space, store, guid), label);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions/{label}")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiOperation(value = "Permanently remove version with given label")
    public ResponseEntity<?> deleteVersion(@PathVariable final String space,
                                                @PathVariable final String store,
                                                @PathVariable final String guid,
                                                @PathVariable final String label) {
        versionHistoryService.deleteVersion(createNodeRef(space, store, guid), label);
        return ResponseEntity.ok().build();
    }
}
