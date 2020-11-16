package eu.xenit.apix.tests.permissions;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.NodePermission;
import eu.xenit.apix.permissions.NodePermission.Access;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.tests.BaseTest;
import java.util.HashSet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;



/**
 * Created by kenneth on 10.03.16.
 */
public class PermissionServiceTest extends BaseTest {

  private final static Logger logger = LoggerFactory.getLogger(PermissionServiceTest.class);
  private static final String ADMIN_USER_NAME = "admin";

  @Autowired
  private ApixToAlfrescoConversion c;

  @Autowired
  private IPermissionService service;

  @Autowired
  @Qualifier("PermissionService")
  private PermissionService permissionService;

  @Autowired
  @Qualifier("SearchService")
  private SearchService searchService;

  @Autowired
  @Qualifier("NodeService")
  private NodeService alfrescoNodeService;

  // Low level nodeservice bypasses permissionchecks
  @Autowired
  @Qualifier("nodeService")
  private NodeService llAlfrescoNodeService;

  @Autowired
  @Qualifier("FileFolderService")
  private FileFolderService fileFolderService;

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
  public void testGetPermissions() {
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");

    try {
      Map<String, PermissionValue> permissions = service
          .getPermissions(c.apix(testNode.getNodeRef()));
      logger.debug(permissions.toString());
      assertTrue(permissions.containsKey("Read"));
      assertTrue(permissions.containsKey("Write"));
      assertTrue(permissions.containsKey("Delete"));
      assertEquals(permissions.get("Read"), PermissionValue.ALLOW);
      assertEquals(permissions.get("Write"), PermissionValue.ALLOW);
      assertEquals(permissions.get("Delete"), PermissionValue.ALLOW);
    } finally {
      removeTestNode(mainTestFolder.getNodeRef());
    }
  }

  @Test
  public void testGetPermissionsV2() {
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");

    try {
      Map<String, PermissionValue> permissions = service
          .getPermissionsFast(c.apix(testNode.getNodeRef()));
      logger.debug(permissions.toString());
      assertTrue(permissions.containsKey("Read"));
      assertTrue(permissions.containsKey("Write"));
      assertTrue(permissions.containsKey("Delete"));
      assertEquals(permissions.get("Read"), PermissionValue.ALLOW);
      assertEquals(permissions.get("Write"), PermissionValue.ALLOW);
      assertEquals(permissions.get("Delete"), PermissionValue.ALLOW);
    } finally {
      removeTestNode(mainTestFolder.getNodeRef());
    }
  }

  @Test
  public void testSetPermissions() {
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");
    Long initialAclId = llAlfrescoNodeService.getNodeAclId(testNode.getNodeRef());

    try {
      Set<Access> accessSet = new HashSet<>();
      Access access = new Access();
      access.setAuthority("abeecher");
      access.setPermission("Contributor");
      accessSet.add(access);
      NodePermission nodePermission = new NodePermission(true, accessSet, null);

      service.setNodePermissions(c.apix(testNode.getNodeRef()), nodePermission);

      //check if the effect of setting the permissions is reached
      NodePermission nodePermission1 = service
          .getNodePermissions(c.apix(testNode.getNodeRef()));
      assertEquals(nodePermission.isInheritFromParent(),
          nodePermission1.isInheritFromParent());
      assertEquals(nodePermission.getOwnAccessList().size(),
          nodePermission1.getOwnAccessList().size());
      Access access1 = (Access) nodePermission1.getOwnAccessList().toArray()[0];
      assertEquals(access.getAuthority(), access1.getAuthority());
      assertEquals(access.getPermission(), access1.getPermission());
      assertEquals(access.isAllowed(), access1.isAllowed());
      assertNotEquals(initialAclId, llAlfrescoNodeService.getNodeAclId(testNode.getNodeRef()));

      //reset the nodes permissions
      service.setNodePermissions(c.apix(testNode.getNodeRef()),
          new NodePermission(true, new HashSet<Access>(), null));

      //check if the aclId is set back to the initial one.
      assertEquals(initialAclId, llAlfrescoNodeService.getNodeAclId(testNode.getNodeRef()));


    } finally {
      removeTestNode(mainTestFolder.getNodeRef());
    }
  }
}
