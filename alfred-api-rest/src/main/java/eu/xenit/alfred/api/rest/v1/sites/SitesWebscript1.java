package eu.xenit.alfred.api.rest.v1.sites;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import eu.xenit.alfred.api.rest.v1.nodes.NodeInfo;
import eu.xenit.alfred.api.sites.ISite;
import eu.xenit.alfred.api.sites.ISiteService;
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

@AlfrescoTransaction
@RestController
public class SitesWebscript1 extends AlfredApiV1Webscript {

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

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/sites/mySites")
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
