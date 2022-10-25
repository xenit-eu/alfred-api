package eu.xenit.apix.rest.v1.sites;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.sites.ISite;
import eu.xenit.apix.sites.ISiteService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
//        description = "Access operations on sites", value = "Sites")
//@Transaction(readOnly = false)
@RestController("eu.xenit.apix.rest.v1.SitesWebscript")
public class SitesWebscript1 extends ApixV1Webscript {

    private static final Logger logger = LoggerFactory.getLogger(SitesWebscript1.class);

    private final INodeService nodeService;

    private final IPermissionService permissionService;

    private final IFileFolderService fileFolderService;

    private final ISiteService siteService;

    private final ServiceRegistry serviceRegistry;

    public SitesWebscript1(INodeService nodeService, IPermissionService permissionService,
                           IFileFolderService fileFolderService, ISiteService siteService,
                           ServiceRegistry serviceRegistry) {
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.fileFolderService = fileFolderService;
        this.siteService = siteService;
        this.serviceRegistry = serviceRegistry;
    }

    @ApiOperation(value = "Retrieves information about the available sites of the current user",
            notes = "Returns a list of sites. For each site the node reference, short name, title, description,\n"
                    + "site visibility and list of site components (document libray, links, data lists, wiki,\n"
                    + "discussions) are returned.\n"
                    + "\n"
                    + "There are no mandatory request parameters. However, there are optional ones:\n"
                    + "Set 'retrieveMetadata' to true to return the aspects and properties of the sites.\n"
                    + "Set 'retrievePath' to true to return the path of the sites.\n"
                    + "Set 'retrievePermissions' to true to return the permissions of the sites.\n"
                    + "Set 'retrieveChildAssocs' to true to return the child associations of the sites.\n"
                    + "Set 'retrieveParentAssocs' to true to return the parent associations of the sites.\n"
                    + "Set 'retrieveTargetAssocs' to true to return the target peer associations of the sites.\n"
                    + "Set 'retrieveSourceAssocs' to true to return the source peer associations of the sites.\n")
    @GetMapping(value = "/v1/sites/mySites")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SiteInfo[].class))
    public ResponseEntity<List<SiteInfo>> getMySites(
             @RequestParam(required = false, defaultValue = "false") Boolean retrieveMetadata,
             @RequestParam(required = false, defaultValue = "false") boolean retrievePath,
             @RequestParam(required = false, defaultValue = "false") boolean retrievePermissions,
             @RequestParam(required = false, defaultValue = "false") boolean retrieveChildAssocs,
             @RequestParam(required = false, defaultValue = "false") boolean retrieveParentAssocs,
             @RequestParam(required = false, defaultValue = "false") boolean retrieveTargetAssocs,
             @RequestParam(required = false, defaultValue = "false") boolean retrieveSourceAssocs) {
        logger.debug("retrieveMetadata: {}", retrieveMetadata);
        logger.debug("retrievePath: {}", retrievePath);
        logger.debug("retrievePermissions: {}", retrievePermissions);
        logger.debug("retrieveChildAssocs: {}", retrieveChildAssocs);
        logger.debug("retrieveParentAssocs: {}", retrieveParentAssocs);
        logger.debug("retrieveTargetAssocs: {}", retrieveTargetAssocs);
        logger.debug("retrieveSourceAssocs: {}", retrieveSourceAssocs);

        AuthenticationService authService = serviceRegistry.getAuthenticationService();
        List<ISite> sites = siteService.getUserSites(authService.getCurrentUserName());
        List<SiteInfo> siteInfoList = new ArrayList<>();
        for (ISite site : sites) {
            NodeRef siteRef = site.getNodeRef();
            NodeInfo nodeInfo = nodeRefToNodeInfo(siteRef, fileFolderService, nodeService, permissionService,
                    retrievePath, retrieveMetadata, retrievePermissions, true, retrieveChildAssocs,
                    retrieveParentAssocs, retrieveTargetAssocs, retrieveSourceAssocs);
            SiteInfo siteInfo = new SiteInfo(site, nodeInfo);
            siteInfoList.add(siteInfo);
        }

        return writeJsonResponse(siteInfoList);
    }
}
