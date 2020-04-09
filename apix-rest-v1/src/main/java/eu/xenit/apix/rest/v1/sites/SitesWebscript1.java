package eu.xenit.apix.rest.v1.sites;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.sites.ISite;
import eu.xenit.apix.sites.ISiteService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Access operations on sites", value = "Sites")
@Transaction(readOnly = false)
@Component("eu.xenit.apix.rest.v1.SitesWebscript")
public class SitesWebscript1 extends ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(SitesWebscript1.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    IPermissionService permissionService;

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    ISiteService siteService;

    @Autowired
    ServiceRegistry serviceRegistry;

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
                    +"Set 'retrieveTargetAssocs' to true to return the peer associations of the sites.\n")
    @Uri(value = "/sites/mySites", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = SiteInfo[].class))
    public void getMySites(@RequestParam(required = false) Boolean retrieveMetadata,
            @RequestParam(required = false) Boolean retrievePath,
            @RequestParam(required = false) Boolean retrievePermissions,
            @RequestParam(required = false) Boolean retrieveChildAssocs,
            @RequestParam(required = false) Boolean retrieveParentAssocs,
            @RequestParam(required = false) Boolean retrieveTargetAssocs,
            WebScriptResponse response)
            throws IOException {
        logger.debug("retrieveMetadata: "+retrieveMetadata);
        logger.debug("retrievePath: "+retrievePath);
        logger.debug("retrievePermissions: "+retrievePermissions);
        logger.debug("retrieveChildAssocs: "+retrieveChildAssocs);
        logger.debug("retrieveParentAssocs: "+retrieveParentAssocs);
        logger.debug("retrieveTargetAssocs: "+retrieveTargetAssocs);

        if (retrieveMetadata == null){
            retrieveMetadata = false;
        }
        if (retrievePath == null){
            retrievePath = false;
        }
        if (retrievePermissions == null){
            retrievePermissions = false;
        }
        if (retrieveChildAssocs == null){
            retrieveChildAssocs = false;
        }
        if (retrieveParentAssocs == null){
            retrieveParentAssocs = false;
        }
        if (retrieveTargetAssocs == null){
            retrieveTargetAssocs = false;
        }

        AuthenticationService authService = serviceRegistry.getAuthenticationService();
        List<ISite> sites = siteService.getUserSites(authService.getCurrentUserName());
        List<SiteInfo> siteInfoList = new ArrayList<>();
        for (ISite site : sites) {
            NodeRef siteRef = site.getNodeRef();
            NodeInfo nodeInfo = nodeRefToNodeInfo(siteRef, fileFolderService, nodeService, permissionService,
                    retrievePath, retrieveMetadata, retrievePermissions, true, retrieveChildAssocs,
                    retrieveParentAssocs, retrieveTargetAssocs);
            SiteInfo siteInfo = new SiteInfo(site, nodeInfo);
            siteInfoList.add(siteInfo);
        }

        writeJsonResponse(response, siteInfoList);
    }
}
