package eu.xenit.apix.tests.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.NodePermission;
import eu.xenit.apix.permissions.NodePermission.Access;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.server.ApplicationContextProvider;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.util.SolrTestHelperImpl;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;



/**
 * Created by kenneth on 10.03.16.
 */
public class PermissionServiceTest extends BaseTest {

  private final static Logger logger = LoggerFactory.getLogger(PermissionServiceTest.class);
  private static final String ADMIN_USER_NAME = "admin";

  private ApplicationContext testApplicationContext;
  private ApixToAlfrescoConversion c;
  private IPermissionService service;
  private PermissionService permissionService;
  private SearchService searchService;
  private ServiceRegistry serviceRegistry;
  private NodeService alfrescoNodeService;

  private NodeService llAlfrescoNodeService;
  private FileFolderService fileFolderService;
  private SolrTestHelperImpl solrHelper;
  @Before
  public void Setup() {
    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    // initialiseBeans BaseTest
    initialiseBeans();
    // initialise the local beans
    testApplicationContext = ApplicationContextProvider.getApplicationContext();
    serviceRegistry = (ServiceRegistry) testApplicationContext.getBean(ServiceRegistry.class);
    c =  (ApixToAlfrescoConversion) testApplicationContext.getBean(ApixToAlfrescoConversion.class);
    service = (IPermissionService) testApplicationContext.getBean(IPermissionService.class);
    permissionService = serviceRegistry.getPermissionService();
    searchService = serviceRegistry.getSearchService();
    fileFolderService = serviceRegistry.getFileFolderService();
    alfrescoNodeService = serviceRegistry.getNodeService();
    llAlfrescoNodeService = (NodeService) testApplicationContext.getBean("nodeService",NodeService.class);
    solrHelper = testApplicationContext.getBean(SolrTestHelperImpl.class);
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
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }
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
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }

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
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }
    cleanUp();

    NodeRef companyHomeNodeRef = getNodeAtPath("/app:company_home");
    FileInfo mainTestFolder = createMainTestFolder(companyHomeNodeRef);
    FileInfo testNode = createTestNode(mainTestFolder.getNodeRef(), "testnode");
    Long initialAclId = llAlfrescoNodeService.getNodeAclId(testNode.getNodeRef());
    try {
      solrHelper.waitForTransactionSync();
    } catch (InterruptedException e) {
      Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
    }

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
