package eu.xenit.apix.tests.versionhistory;

import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.alfresco.service.cmr.model.FileInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class VersionHistoryServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(VersionHistoryServiceTest.class);
    @Autowired
    private IVersionHistoryService versionHistoryService;

    @Autowired
    private org.alfresco.service.cmr.version.VersionService alfrizcoVersionHistoryService;


    //Test variables
    private NodeRef testNode;


    public void Setup() {
        this.cleanUp();
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
        testNode = this.createTestNode(mainTestFolder.getNodeRef(), "testnode").getNodeRef();
    }


    @Test
    public void TestGetVersionHistory() {
        Setup();

        Map<String, Serializable> versionProperties = new HashMap<>();

        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));

        assertEquals(beforeVersioning, null);

        //First a minor version
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MINOR);
        org.alfresco.service.cmr.version.Version version = alfrizcoVersionHistoryService
                .createVersion(testNode, versionProperties);
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));

        assertNotEquals(initialVersioning, null);
        assertEquals(initialVersioning.getVersionHistory().size(), 1);
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
        assertEquals(secondVersioning.getVersionHistory().size(), 2);
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }

    @Test
    public void TestCreateVersion() {
        Setup();
        Map<String, Serializable> versionProperties = new HashMap<>();

        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertEquals(beforeVersioning, null);

        //First a minor version
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MINOR);
        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));

        assertNotEquals(initialVersioning, null);
        assertEquals(initialVersioning.getVersionHistory().size(), 1);
        Version firstVersion = initialVersioning.getVersionHistory().get(0);
        assertNotEquals(firstVersion.getModifiedDate(), null);
        assertEquals(Version.VersionType.MINOR, firstVersion.getType());

        //Now a major version with comment
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MAJOR);
        versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
        VersionHistory secondVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(secondVersioning, null);
        assertEquals(secondVersioning.getVersionHistory().size(), 2);
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }
}
