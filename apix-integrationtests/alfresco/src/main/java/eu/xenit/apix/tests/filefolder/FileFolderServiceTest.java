package eu.xenit.apix.tests.filefolder;

//import com.github.dynamicextensionsalfresco.webscripts.annotations.Before; // TODO switch to before of junit,
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.tests.BaseTest;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.*;

/**
 * Created by kenneth on 11.03.16.
 */
public class FileFolderServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);

    private static final String COMPANY_HOME_FOLDER_PATH = "/Company Home/ApixMainTestFolder";
    private static final String TEST_NODE_QNAME_PATH = "/app:company_home/cm:ApixMainTestFolder/cm:testnode";


    StoreRef alfStoreRef = new StoreRef("workspace", "SpacesStore");
    eu.xenit.apix.data.StoreRef apixStoreRef = new eu.xenit.apix.data.StoreRef("workspace", "SpacesStore");

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private ApixToAlfrescoConversion c;

    @Autowired
    private IFileFolderService service;

    @Autowired
    @Qualifier("FileFolderService")
    private FileFolderService fileFolderService;

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;

    @Autowired
    @Qualifier("NodeService")
    private NodeService alfrescoNodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private Repository repository;
    // TODO - validate behaviour is the same as with
    // com.github.dynamicextensionsalfresco.webscripts.annotations.Before;
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
        String displayPath = this.service.getPath(c.apix(testNode.getNodeRef())).getDisplayPath();
        logger.debug("DisplayPath: " + displayPath);
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
        String qNamePath = this.service.getPath(c.apix(testNode.getNodeRef())).getQnamePath();
        logger.debug("QNamePath: " + qNamePath);
        assertNotNull(qNamePath);
        assertEquals(qNamePath, TEST_NODE_QNAME_PATH);
    }

    @Test
    public void testRootFolder() throws Exception {
        eu.xenit.apix.data.StoreRef storeRef = new eu.xenit.apix.data.StoreRef("workspace", "SpacesStore");
        eu.xenit.apix.data.NodeRef folderNodeRef = this.service.getRootFolder(storeRef);
        //assertEquals("workspace://SpacesStore/3817fd61-e809-4e2a-a8cb-a420f38aed05", getChildNodeRef.toString());

        eu.xenit.apix.filefolder.NodePath path = this.service.getPath(folderNodeRef);
        assertEquals("", path.getDisplayPath());
    }

    @Test
    public void testCreateFolder() {
        this.cleanUp();
        NodeRef companyHomeRef = repository.getCompanyHome();
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeRef);
        FileInfo testFolder = this.createTestFolder(mainTestFolder.getNodeRef(), "parentfolder");

        try {
            eu.xenit.apix.data.NodeRef parentNodeRef = c.apix(testFolder.getNodeRef());
            String folderName = "testFolder";

            assertFalse(this.service.existsFolder(parentNodeRef, folderName));

            eu.xenit.apix.data.NodeRef folderNodeRef = this.service
                    .createFolder(c.apix(testFolder.getNodeRef()), folderName);

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
            eu.xenit.apix.data.NodeRef parentNodeRef = c.apix(testFolder.getNodeRef());
            eu.xenit.apix.data.NodeRef parentSubFolderNodeRef = c.apix(testSubFolder.getNodeRef());
            String folderName = "testDeepFolder";

            assertTrue(this.service.existsFolder(parentNodeRef, parentSubFolderName));
            assertFalse(this.service.existsFolder(parentSubFolderNodeRef, folderName));

            eu.xenit.apix.data.NodeRef folderNodeRef = this.service.createFolder(parentSubFolderNodeRef, folderName);

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
        eu.xenit.apix.data.NodeRef apixRef = service.getCompanyHome();
        eu.xenit.apix.data.NodeRef check = c.apix(alfRef);
        assertTrue(apixRef.getGuid().equals(check.getGuid()));
        assertTrue(apixRef.getStoreRefId().equals(check.getStoreRefId()));
        assertTrue(apixRef.getStoreRefProtocol().equals(check.getStoreRefProtocol()));
        assertTrue(apixRef.toString().equals(check.toString()));
    }

    @Test
    public void TestGetRootFolder() {
        assertEquals(
                service.getRootFolder(apixStoreRef).toString(),
                alfrescoNodeService.getRootNode(alfStoreRef).toString());
    }

}
