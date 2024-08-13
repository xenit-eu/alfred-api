package eu.xenit.alfred.api.alfresco.sites;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.sites.ISite;
import eu.xenit.alfred.api.sites.ISiteService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.alfresco.sites.SiteService")
public class SiteService implements ISiteService {

    private final static Logger logger = LoggerFactory.getLogger(SiteService.class);
    private final static String DOCUMENT_LIBRARY_COMPONENT = "documentLibrary";
    private final static String LINKS_COMPONENT = "links";
    private final static String DATA_LISTS_COMPONENT = "dataLists";
    private final static String WIKI_COMPONENT = "wiki";
    private final static String DISCUSSIONS_COMPONENT = "discussions";

    private AlfredApiToAlfrescoConversion c;
    private ServiceRegistry serviceRegistry;

    @Autowired
    public SiteService(ServiceRegistry serviceRegistry, AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion) {
        this.serviceRegistry = serviceRegistry;
        c = alfredApiToAlfrescoConversion;
    }

    public List<ISite> getUserSites(String userId) {
        org.alfresco.service.cmr.site.SiteService siteService = serviceRegistry.getSiteService();

        List<ISite> alfredApiSites = new ArrayList<>();
        List<SiteInfo> userSites = siteService.listSites(userId);

        for (SiteInfo userSite : userSites) {

            NodeRef nodeRef = c.alfredApi(userSite.getNodeRef());
            String shortName = userSite.getShortName();
            String title = userSite.getTitle();
            String description = userSite.getDescription();
            boolean isPublic = userSite.getIsPublic();
            // Wrapping method for NYCSANSUP-34:
            // Alfresco-RM blocked retrieval of components due to its onw permission model
            // this caused the entire call to fail.
            try {
                Map<String, NodeRef> componentsMap = getSiteComponents(siteService, shortName);
                alfredApiSites.add(new Site(nodeRef, shortName, title, description, isPublic, componentsMap));
            } catch (AccessDeniedException accessDeniedException) {
                logger.warn("User {} does not have access to a site component for site {} according to exception.",
                        userId, shortName, accessDeniedException);
            }
        }
        return alfredApiSites;
    }

    private Map<String, NodeRef> getSiteComponents(org.alfresco.service.cmr.site.SiteService siteService, String siteShortname) {
        Map<String, NodeRef> componentsMap = new HashMap<>();
        addSiteComponentToMap(siteService, componentsMap, siteShortname, DOCUMENT_LIBRARY_COMPONENT);
        addSiteComponentToMap(siteService, componentsMap, siteShortname, LINKS_COMPONENT);
        addSiteComponentToMap(siteService, componentsMap, siteShortname, DATA_LISTS_COMPONENT);
        addSiteComponentToMap(siteService, componentsMap, siteShortname, WIKI_COMPONENT);
        addSiteComponentToMap(siteService, componentsMap, siteShortname, DISCUSSIONS_COMPONENT);

        return componentsMap;
    }

    private void addSiteComponentToMap(org.alfresco.service.cmr.site.SiteService siteService, Map<String, NodeRef> componentsMap,
                                       String siteShortname, String siteComponentName) {
        try {
            NodeRef componentRef = c.alfredApi(siteService.getContainer(siteShortname, siteComponentName));
            if (componentRef == null) {
                return;
            }
            componentsMap.put(siteComponentName, componentRef);
        }
        catch (AccessDeniedException ex) {
            logger.debug("Access denied to site component {} for site {}. It is not added to the map.", siteComponentName, siteShortname);
        }
    }
}
