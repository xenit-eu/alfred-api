package eu.xenit.apix.tests.sites;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.sites.ISite;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GetUserSitesUnitTest {
    private ServiceRegistry serviceRegistry;
    private ApixToAlfrescoConversion apixAlfrescoConverter;
    private SiteInfo testSite1;
    private SiteInfo testSite2;
    private SiteInfo testSite3;

    private static final String TEST_SITE_1_REF = "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bce";
    private static final String TEST_SITE_2_REF = "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bcf";
    private static final String TEST_SITE_3_REF = "workspapce://SpacesStore/d1ef44c4-5bd3-457a-9b08-abd23d588bd0";

    private static final String TEST_SITE_1_SHORT_NAME = "testSite1";
    private static final String TEST_SITE_2_SHORT_NAME = "testSite2";
    private static final String TEST_SITE_3_SHORT_NAME = "testSite3";

    private final static String DOCUMENT_LIBRARY_COMPONENT = "documentLibrary";
    private final static String LINKS_COMPONENT = "links";
    private final static String DATA_LISTS_COMPONENT = "dataLists";
    private final static String WIKI_COMPONENT = "wiki";
    private final static String DISCUSSIONS_COMPONENT = "discussions";

    @Before
    public void init() {
        serviceRegistry = mock(ServiceRegistry.class);
        SiteService siteServiceMock = initSiteServiceMock();
        when(serviceRegistry.getSiteService()).thenReturn(siteServiceMock);

        apixAlfrescoConverter = new ApixToAlfrescoConversion(serviceRegistry);
    }

    private SiteService initSiteServiceMock() {
        SiteService siteServiceMock = mock(SiteService.class);

        //Initializing sites
        List<SiteInfo> sites = initSites(siteServiceMock);

        //Init mock of listSites method
        when(siteServiceMock.listSites(Mockito.anyString())).thenReturn(sites);

        return siteServiceMock;
    }

    private List<SiteInfo> initSites(SiteService siteService) {
        List<SiteInfo> sites = new ArrayList<>();
        testSite1 = createSite(TEST_SITE_1_SHORT_NAME, TEST_SITE_1_SHORT_NAME, TEST_SITE_1_SHORT_NAME,
                new NodeRef(TEST_SITE_1_REF));
        initSiteComponents(siteService, testSite1);
        sites.add(testSite1);
        testSite2 = createSite(TEST_SITE_2_SHORT_NAME, TEST_SITE_2_SHORT_NAME, TEST_SITE_2_SHORT_NAME,
                new NodeRef(TEST_SITE_2_REF));
        initSiteComponents(siteService, testSite2);
        sites.add(testSite2);
        testSite3 = createSite(TEST_SITE_3_SHORT_NAME, TEST_SITE_3_SHORT_NAME, TEST_SITE_3_SHORT_NAME,
                new NodeRef(TEST_SITE_3_REF));
        initSiteComponents(siteService, testSite3);
        sites.add(testSite3);

        return sites;
    }

    private SiteInfo createSite(String shortName, String title, String description, NodeRef nodeRef) {
        SiteInfo siteMock = mock(SiteInfo.class);
        when(siteMock.getShortName()).thenReturn(shortName);
        when(siteMock.getTitle()).thenReturn(title);
        when(siteMock.getDescription()).thenReturn(description);
        when(siteMock.getVisibility()).thenReturn(SiteVisibility.PUBLIC);
        when(siteMock.getNodeRef()).thenReturn(nodeRef);

        return siteMock;
    }

    private void initSiteComponents(SiteService siteService, SiteInfo site) {
        String shortName = site.getShortName();
        NodeRef documentLibraryComponentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, shortName + "docLib");
        NodeRef linksComponentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, shortName + "links");
        NodeRef dataListsComponentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, shortName + "dataLists");
        NodeRef wikiComponentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, shortName + "wiki");
        NodeRef discussionsComponentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, shortName + "discussions");

        when(siteService.getContainer(site.getShortName(), DOCUMENT_LIBRARY_COMPONENT))
                .thenReturn(documentLibraryComponentRef);
        when(siteService.getContainer(site.getShortName(), LINKS_COMPONENT))
                .thenReturn(linksComponentRef);
        when(siteService.getContainer(site.getShortName(), DATA_LISTS_COMPONENT))
                .thenReturn(dataListsComponentRef);
        when(siteService.getContainer(site.getShortName(), WIKI_COMPONENT))
                .thenReturn(wikiComponentRef);
        when(siteService.getContainer(site.getShortName(), DISCUSSIONS_COMPONENT))
                .thenReturn(discussionsComponentRef);
    }

    @Test
    public void testGetUserSites() {
        SiteService alfrescoSiteService = serviceRegistry.getSiteService();
        eu.xenit.apix.alfresco.sites.SiteService apixSiteService =
                new eu.xenit.apix.alfresco.sites.SiteService(serviceRegistry, apixAlfrescoConverter);

        String user = "testUser";
        List<ISite> testUserSites = apixSiteService.getUserSites(user);
        Assert.assertNotEquals(null, testSite1);
        Assert.assertNotEquals(null, testSite2);
        Assert.assertNotEquals(null, testSite3);
        Assert.assertEquals(3, testUserSites.size());
        Assert.assertEquals(testSite1.getShortName(), testUserSites.get(0).getShortName());
        Assert.assertEquals(testSite2.getShortName(), testUserSites.get(1).getShortName());
        Assert.assertEquals(testSite3.getShortName(), testUserSites.get(2).getShortName());
        verify(alfrescoSiteService, times(1)).listSites(eq(user));
        verifyGetContainer(alfrescoSiteService, testSite1);
        verifyGetContainer(alfrescoSiteService, testSite2);
        verifyGetContainer(alfrescoSiteService, testSite3);
    }

    private void verifyGetContainer(SiteService alfrescoSiteService, SiteInfo testSite) {
        verify(alfrescoSiteService, times(1)).getContainer(eq(testSite.getShortName()), eq(DOCUMENT_LIBRARY_COMPONENT));
        verify(alfrescoSiteService, times(1)).getContainer(eq(testSite.getShortName()), eq(LINKS_COMPONENT));
        verify(alfrescoSiteService, times(1)).getContainer(eq(testSite.getShortName()), eq(DATA_LISTS_COMPONENT));
        verify(alfrescoSiteService, times(1)).getContainer(eq(testSite.getShortName()), eq(WIKI_COMPONENT));
        verify(alfrescoSiteService, times(1)).getContainer(eq(testSite.getShortName()), eq(DISCUSSIONS_COMPONENT));
    }
}
