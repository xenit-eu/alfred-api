package eu.xenit.apix.alfresco.sites;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.sites.ISite;
import eu.xenit.apix.sites.ISiteService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.alfresco.sites.SiteService")
public class SiteService implements ISiteService {

    private final static Logger logger = LoggerFactory.getLogger(SiteService.class);
    private final static String DOCUMENT_LIBRARY_COMPONENT = "documentLibrary";
    private final static String LINKS_COMPONENT = "links";
    private final static String DATA_LISTS_COMPONENT = "dataLists";
    private final static String WIKI_COMPONENT = "wiki";
    private final static String DISCUSSIONS_COMPONENT = "discussions";

    private ApixToAlfrescoConversion c;
    private ServiceRegistry serviceRegistry;

    @Autowired
    public SiteService(ServiceRegistry serviceRegistry, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        this.serviceRegistry = serviceRegistry;
        c = apixToAlfrescoConversion;
    }

    public List<ISite> getUserSites(String userId) {
        org.alfresco.service.cmr.site.SiteService siteService = serviceRegistry.getSiteService();

        List<ISite> apixSites = new ArrayList<>();
        List<SiteInfo> userSites = siteService.listSites(userId);

        for (SiteInfo userSite : userSites) {

                NodeRef nodeRef = c.apix(userSite.getNodeRef());
                String shortName = userSite.getShortName();
                String title = userSite.getTitle();
                String description = userSite.getDescription();
                boolean isPublic = userSite.getIsPublic();
                Map<String, NodeRef> componentsMap = getSiteComponents(siteService, shortName);
                apixSites.add(new Site(nodeRef, shortName, title, description, isPublic, componentsMap));
        }

        return apixSites;
    }

    private Map<String, NodeRef> getSiteComponents(org.alfresco.service.cmr.site.SiteService siteService, String siteShortname) {
        Map<String, NodeRef> componentsMap = new HashMap<>();
        componentsMap.put(DOCUMENT_LIBRARY_COMPONENT, getSiteComponent(siteService, siteShortname, DOCUMENT_LIBRARY_COMPONENT));
        componentsMap.put(LINKS_COMPONENT, getSiteComponent(siteService, siteShortname, LINKS_COMPONENT));
        componentsMap.put(DATA_LISTS_COMPONENT, getSiteComponent(siteService, siteShortname, DATA_LISTS_COMPONENT));
        componentsMap.put(WIKI_COMPONENT, getSiteComponent(siteService, siteShortname, WIKI_COMPONENT));
        componentsMap.put(DISCUSSIONS_COMPONENT, getSiteComponent(siteService, siteShortname, DISCUSSIONS_COMPONENT));
        return componentsMap;
    }

    private NodeRef getSiteComponent(org.alfresco.service.cmr.site.SiteService siteService, String siteShortname, String componentName) {
        NodeRef component = null;
        // Wrapping call because Alfresco RM or other components my intercept and cause a failure.
        try {
            component = c.apix(siteService.getContainer(siteShortname, componentName));
        } catch (AccessDeniedException accessDeniedException) {
            logger.error("User does not have permission to component {} for site {} due to following error.", componentName, siteShortname, accessDeniedException);
        }
        return null;
    }
}
