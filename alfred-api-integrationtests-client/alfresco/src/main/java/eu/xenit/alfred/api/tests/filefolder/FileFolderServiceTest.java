package eu.xenit.alfred.api.tests.filefolder;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.filefolder.NodePath;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import eu.xenit.alfred.api.util.SolrTestHelperImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kenneth on 11.03.16.
 */
public class FileFolderServiceTest extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(FileFolderServiceTest.class);

    private static final String COMPANY_HOME_FOLDER_PATH = "/Company Home/AlfredApiMainTestFolder";
    private static final String TEST_NODE_QNAME_PATH = "/app:company_home/cm:AlfredApiMainTestFolder/cm:testnode";


    StoreRef alfStoreRef = new StoreRef("workspace", "SpacesStore");
    eu.xenit.alfred.api.data.StoreRef alfredApiStoreRef = new eu.xenit.alfred.api.data.StoreRef("workspace", "SpacesStore");

    private final IFileFolderService service;
    private final FileFolderService fileFolderService;
    private final SearchService searchService;
    private final NodeService alfrescoNodeService;
    private final ContentService contentService;
    private final SolrTestHelperImpl solrHelper;


    public FileFolderServiceTest() {
        // initialise the local beans
        service = getBean(IFileFolderService.class);
        fileFolderService = serviceRegistry.getFileFolderService();
        searchService = serviceRegistry.getSearchService();
        alfrescoNodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
        solrHelper = getBean(SolrTestHelperImpl.class);
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    public NodeRef getNodeAtPath(String path) {
        StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, path);
        NodeRef companyHomeNodeRef = null;
        try {
            if (rs.length() == 0) {
                throw new RuntimeException("Didn't find node at: " + path);
            }
            companyHomeNodeRef = rs.getNodeRef(0);
        } finally {
            rs.close();
        }
        return companyHomeNodeRef;
    }

    @Test
    public void TestGetDisplayPath() {
        try {
            solrHelper.waitForTransactionSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        this.cleanUp();
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");

        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
        FileInfo testNode = this.createTestNode(mainTestFolder.getNodeRef(), "testnode");
        try {
            solrHelper.waitForTransactionSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        String displayPath = this.service.getPath(c.alfredApi(testNode.getNodeRef())).getDisplayPath();
        logger.error("DisplayPath: " + displayPath);
        assertNotNull(displayPath);
        assertEquals(displayPath, COMPANY_HOME_FOLDER_PATH);
    }

    @Test
    public void TestGetQNamePath() {
        try {
            solrHelper.waitForTransactionSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        this.cleanUp();

        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
        FileInfo testNode = this.createTestNode(mainTestFolder.getNodeRef(), "testnode");
        try {
            solrHelper.waitForTransactionSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        String qNamePath = this.service.getPath(c.alfredApi(testNode.getNodeRef())).getQnamePath();
        logger.debug("QNamePath: " + qNamePath);
        assertNotNull(qNamePath);
        assertEquals(qNamePath, TEST_NODE_QNAME_PATH);
    }

    @Test
    public void testRootFolder() throws Exception {
        eu.xenit.alfred.api.data.StoreRef storeRef = new eu.xenit.alfred.api.data.StoreRef("workspace", "SpacesStore");
        eu.xenit.alfred.api.data.NodeRef folderNodeRef = this.service.getRootFolder(storeRef);
        //assertEquals("workspace://SpacesStore/3817fd61-e809-4e2a-a8cb-a420f38aed05", getChildNodeRef.toString());

        NodePath path = this.service.getPath(folderNodeRef);
        assertEquals("", path.getDisplayPath());
    }

    @Test
    public void testCreateFolder() {
        this.cleanUp();
        NodeRef companyHomeRef = repository.getCompanyHome();
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeRef);
        FileInfo testFolder = this.createTestFolder(mainTestFolder.getNodeRef(), "parentfolder");

        try {
            eu.xenit.alfred.api.data.NodeRef parentNodeRef = c.alfredApi(testFolder.getNodeRef());
            String folderName = "testFolder";

            assertFalse(this.service.existsFolder(parentNodeRef, folderName));

            eu.xenit.alfred.api.data.NodeRef folderNodeRef = this.service
                    .createFolder(c.alfredApi(testFolder.getNodeRef()), folderName);

            assertTrue(this.service.existsFolder(parentNodeRef, folderName));

            assertEquals(folderNodeRef, this.service.getChildNodeRef(parentNodeRef, folderName));
            assertEquals(folderNodeRef, this.service.getChildNodeRef(parentNodeRef, new String[]{folderName}));

            this.service.deleteFolder(folderNodeRef);

            assertFalse(this.service.existsFolder(parentNodeRef, folderName));

        } finally {
            this.removeTestNode(mainTestFolder.getNodeRef());
        }
    }

    @Test
    public void testCreateDeepFolder() {
        this.cleanUp();
        NodeRef companyHomeRef = repository.getCompanyHome();
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeRef);
        FileInfo testFolder = this.createTestFolder(mainTestFolder.getNodeRef(), "deepParentFolder");
        String parentSubFolderName = "deepParentSubFolder";
        FileInfo testSubFolder = this.createTestFolder(testFolder.getNodeRef(), parentSubFolderName);

        try {
            eu.xenit.alfred.api.data.NodeRef parentNodeRef = c.alfredApi(testFolder.getNodeRef());
            eu.xenit.alfred.api.data.NodeRef parentSubFolderNodeRef = c.alfredApi(testSubFolder.getNodeRef());
            String folderName = "testDeepFolder";

            assertTrue(this.service.existsFolder(parentNodeRef, parentSubFolderName));
            assertFalse(this.service.existsFolder(parentSubFolderNodeRef, folderName));

            eu.xenit.alfred.api.data.NodeRef folderNodeRef = this.service.createFolder(parentSubFolderNodeRef, folderName);

            assertEquals(parentSubFolderNodeRef, this.service.getChildNodeRef(parentNodeRef, parentSubFolderName));
            assertEquals(folderNodeRef, this.service.getChildNodeRef(parentSubFolderNodeRef, folderName));
            assertEquals(folderNodeRef,
                    this.service.getChildNodeRef(parentNodeRef, new String[]{parentSubFolderName, folderName}));

            this.service.deleteFolder(folderNodeRef);

            assertFalse(this.service.existsFolder(parentNodeRef, folderName));

        } finally {
            this.removeTestNode(mainTestFolder.getNodeRef());
        }
    }

    @Test
    public void testGetCompanyHome() {
        NodeRef alfRef = repository.getCompanyHome();
        eu.xenit.alfred.api.data.NodeRef alfredApiRef = service.getCompanyHome();
        eu.xenit.alfred.api.data.NodeRef check = c.alfredApi(alfRef);
        assertEquals(alfredApiRef.getGuid(), check.getGuid());
        assertEquals(alfredApiRef.getStoreRefId(), check.getStoreRefId());
        assertEquals(alfredApiRef.getStoreRefProtocol(), check.getStoreRefProtocol());
        assertEquals(alfredApiRef.toString(), check.toString());
    }

    @Test
    public void TestGetRootFolder() {
        assertEquals(
                service.getRootFolder(alfredApiStoreRef).toString(),
                alfrescoNodeService.getRootNode(alfStoreRef).toString());
    }

}
