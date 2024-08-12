package eu.xenit.alfred.api.rest.v1.versionhistory;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import eu.xenit.alfred.api.versionhistory.Version;
import eu.xenit.alfred.api.versionhistory.VersionHistory;
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

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions")
    public ResponseEntity<VersionHistory> getVersionHistory(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        logger.debug("Asked versionhistory for node with guid: {}", guid);
        return writeJsonResponse(
                versionHistoryService.GetVersionHistory(createNodeRef(space, store, guid))
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/root")
    public ResponseEntity<Version> getVersionHistoryRoot(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        return writeJsonResponse(
                versionHistoryService.getRootVersion(createNodeRef(space, store, guid))
        );
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/head")
    public ResponseEntity<Version> getVersionHistoryHead(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        return writeJsonResponse(
                versionHistoryService.getHeadVersion(createNodeRef(space, store, guid))
        );
    }

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/versionhistory/{space}/{store}/{guid}")
    //No method available to disable versioning. deleting will merely reset version history,
    // starting a new history upon a new version change
    public ResponseEntity<?> deleteVersionHistory(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid) {
        versionHistoryService.deleteVersionHistory(createNodeRef(space, store, guid));
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @PutMapping(value = "/v1/versionhistory/{space}/{store}/{guid}")
    public ResponseEntity<?> setVersionHistory(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid,
            @RequestBody(required = false) final VersionOptions versionOptions) {
        HashMap<QName, Serializable> versionProperties = new HashMap<>();
        if (versionOptions != null) {
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

    @AlfrescoTransaction
    @PostMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions/{label}/revert")
    public ResponseEntity<?> revertVersionHistory(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid,
            @PathVariable final String label) {
        versionHistoryService.revert(createNodeRef(space, store, guid), label);
        return ResponseEntity.ok().build();
    }

    @AlfrescoTransaction
    @DeleteMapping(value = "/v1/versionhistory/{space}/{store}/{guid}/versions/{label}")
    public ResponseEntity<?> deleteVersion(@PathVariable final String space,
            @PathVariable final String store,
            @PathVariable final String guid,
            @PathVariable final String label) {
        versionHistoryService.deleteVersion(createNodeRef(space, store, guid), label);
        return ResponseEntity.ok().build();
    }
}
