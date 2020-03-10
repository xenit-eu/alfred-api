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

    @Autowired
    private ServiceRegistry serviceRegistry;

    public SiteService() {
    }

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
            NodeRef documentLibrary = c.apix(siteService.getContainer(shortName, DOCUMENT_LIBRARY_COMPONENT));
            NodeRef links = c.apix(siteService.getContainer(shortName, LINKS_COMPONENT));
            NodeRef dataLists = c.apix(siteService.getContainer(shortName, DATA_LISTS_COMPONENT));
            NodeRef wiki = c.apix(siteService.getContainer(shortName, WIKI_COMPONENT));
            NodeRef discussions = c.apix(siteService.getContainer(shortName, DISCUSSIONS_COMPONENT));
            Map<String, NodeRef> componentsMap = new HashMap<>();
            componentsMap.put(DOCUMENT_LIBRARY_COMPONENT, documentLibrary);
            componentsMap.put(LINKS_COMPONENT, links);
            componentsMap.put(DATA_LISTS_COMPONENT, dataLists);
            componentsMap.put(WIKI_COMPONENT, wiki);
            componentsMap.put(DISCUSSIONS_COMPONENT, discussions);

            apixSites.add(new Site(nodeRef, shortName, title, description, isPublic, componentsMap));
        }

        return apixSites;
    }
}
