package eu.xenit.apix.tests.versionhistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import eu.xenit.apix.tests.JavaApiBaseTest;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionHistoryServiceTestJavaApi extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(VersionHistoryServiceTestJavaApi.class);
    private IVersionHistoryService versionHistoryService;
    private org.alfresco.service.cmr.version.VersionService alfrizcoVersionHistoryService;

    //Test variables
    private NodeRef testNode;

    public VersionHistoryServiceTestJavaApi(){
        versionHistoryService = testApplicationContext.getBean(IVersionHistoryService.class);
        alfrizcoVersionHistoryService = serviceRegistry.getVersionService();
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser("admin");

        this.cleanUp();
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
        testNode = this.createTestNode(mainTestFolder.getNodeRef(), "testnode").getNodeRef();
    }


    @Test
    public void TestGetVersionHistory() {
        Map<String, Serializable> versionProperties = new HashMap<>();
        logger.error("versionHistoryService {}", versionHistoryService);
        logger.error("beforeVersioning {}", versionHistoryService.GetVersionHistory(c.apix(testNode)));
        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertEquals(beforeVersioning.getVersionHistory().size(), 1); // changed from null to 1.

        //First a minor version
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MINOR);
        org.alfresco.service.cmr.version.Version version = alfrizcoVersionHistoryService
                .createVersion(testNode, versionProperties);
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));

        assertNotEquals(initialVersioning, null);
        assertEquals(initialVersioning.getVersionHistory().size(), 2); // changed from 1 -> 2
        Version firstVersion = initialVersioning.getVersionHistory().get(0);

        assertNotEquals(firstVersion.getModifiedDate(), null);
        assertEquals(Version.VersionType.MINOR, firstVersion.getType());

        //Now a major version with comment
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MAJOR);
        versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
        org.alfresco.service.cmr.version.Version version2 = alfrizcoVersionHistoryService
                .createVersion(testNode, versionProperties);
        VersionHistory secondVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(secondVersioning, null);
        assertEquals(secondVersioning.getVersionHistory().size(), 3); // changed from 2 -> 3
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }

    // Fails with System is in RO-mode but we have uploaded the license manually...
    @Test
    public void TestCreateVersion() {
        Map<String, Serializable> versionProperties = new HashMap<>();

        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertEquals(beforeVersioning.getVersionHistory().size(), 1);

        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
//        06150065 Access Denied.  The system is currently in read-only mode.
//      org.alfresco.service.transaction.ReadOnlyServerException: 06150065 Access Denied.  The system is currently in read-only mode.
//        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
        alfrizcoVersionHistoryService.createVersion(testNode, versionProperties);
//        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(initialVersioning, null);
        assertEquals(2, initialVersioning.getVersionHistory().size()); // changed from 1 -> 2
        Version firstVersion = initialVersioning.getVersionHistory().get(0);
        assertNotEquals(firstVersion.getModifiedDate(), null);
        assertEquals(Version.VersionType.MINOR, firstVersion.getType());

        //Now a major version with comment
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MAJOR);
        versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
        // TODO - Apix createversino fails, see error above
//        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
        alfrizcoVersionHistoryService.createVersion(testNode, versionProperties);
        VersionHistory secondVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(secondVersioning, null);
        assertEquals(secondVersioning.getVersionHistory().size(), 3); // 2 to 3??
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }
}