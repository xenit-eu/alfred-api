package eu.xenit.alfred.api.tests.nonadmin;

import static org.alfresco.repo.version.VersionModel.PROP_QNAME_VERSION_LABEL;

import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.NodeMetadata;
import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQueryConsistency;
import eu.xenit.alfred.api.search.SearchQueryResult;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionsTest extends JavaApiBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PermissionsTest.class);

    private static final String USERNAME_NORIGHTS_JOS = "norightsjos";
    private static final String GROUPNAME = "JosFanclub";
    private static final String GROUPID = "GROUP_" + GROUPNAME;
    private static final String PROPERTY_VALUE = "ComeFindMeJos";

    // Alfred API services
    private ISearchService apixSearchService;
    private INodeService apixNodeService;

    // Alfresco services
    private NodeService alfNodeService;
    private PermissionService alfPermissionService;
    private AuthorityService alfAuthorityService;
    private PersonService alfPersonService;
    private MutableAuthenticationService alfAuthenticationService;

    private NodeRef nodeForbidden;
    private NodeRef nodeAllowed;

    public PermissionsTest() {
        apixSearchService = getBean(ISearchService.class);
        apixNodeService = getBean(INodeService.class);

        alfNodeService = getBean("NodeService", NodeService.class);
        alfPermissionService = getBean("PermissionService", PermissionService.class);
        alfAuthorityService = getBean("AuthorityService", AuthorityService.class);
        alfAuthenticationService = getBean("AuthenticationService", MutableAuthenticationService.class);
        alfPersonService = getBean("PersonService", PersonService.class);
    }

    @Before
    public void setup() {
        // For some reason we cannot use @BeforeClass, since this triggers a
        // org.junit.runners.model.InitializationError that cannot be further debugged.

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        try {
            createMainTestFolder(repository.getCompanyHome());
        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping", e);
        }
        createUserAndGroupsWithLimitedRights();

        // Set up folders with group permissions
        try {
            FileInfo folderForbidden = createTestFolder(getMainTestFolder(), "Forbidden");
            alfPermissionService.setInheritParentPermissions(folderForbidden.getNodeRef(), false);
            FileInfo documentForbidden = createTestNode(folderForbidden.getNodeRef(), "ForbiddenDocument");
            nodeForbidden = documentForbidden.getNodeRef();
            alfNodeService.setProperty(nodeForbidden, PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("'Forbidden' folder already created. Skipping", e);
        }

        try {
            FileInfo folderAllowed = createTestFolder(getMainTestFolder(), "Allowed");
            alfPermissionService.setInheritParentPermissions(folderAllowed.getNodeRef(), false);
            alfPermissionService.setPermission(
                    folderAllowed.getNodeRef(), GROUPID, PermissionService.COORDINATOR, true);
            FileInfo documentAllowed = createTestNode(folderAllowed.getNodeRef(), "AllowedDocument");
            nodeAllowed = documentAllowed.getNodeRef();
            alfNodeService.setProperty(nodeAllowed, PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("'Allowed' folder already created. Skipping", e);
        }
    }

    @After
    public void teardown() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        cleanUp();
    }

    private void createUserAndGroupsWithLimitedRights() {
        try {
            alfAuthenticationService.createAuthentication(USERNAME_NORIGHTS_JOS, "foobar".toCharArray());
            Map<QName, Serializable> userProperties = new HashMap<>();
            userProperties.put(ContentModel.PROP_USERNAME, USERNAME_NORIGHTS_JOS);
            userProperties.put(ContentModel.PROP_FIRSTNAME, "Jos");
            userProperties.put(ContentModel.PROP_LASTNAME, "NoRights");
            userProperties.put(ContentModel.PROP_EMAIL, "nojosno@example.com");
            alfAuthenticationService.getAuthenticationEnabled(USERNAME_NORIGHTS_JOS);
            alfPersonService.createPerson(userProperties);
            logger.info("User " + USERNAME_NORIGHTS_JOS + " successfully created");
        } catch (AuthenticationException e) {
            logger.warn("User already created. Skipping", e);
        }

        try {
            alfAuthorityService.createAuthority(AuthorityType.GROUP, GROUPNAME);
            alfAuthorityService.addAuthority(GROUPID, USERNAME_NORIGHTS_JOS);
            logger.info("Group " + GROUPNAME + " successfully created");
        } catch (DuplicateChildNodeNameException e) {
            logger.warn("Group already created. Skipping", e);
        }
    }

    @Test
    public void testSearch() {
        // Switch to non-admin user
        AuthenticationUtil.setFullyAuthenticatedUser(USERNAME_NORIGHTS_JOS);

        SearchQuery query = new SearchQuery();
        query.setQuery(new QueryBuilder()
                .property(PROP_QNAME_VERSION_LABEL.toString(), PROPERTY_VALUE, true)
                .create());
        query.setConsistency(SearchQueryConsistency.TRANSACTIONAL);
        SearchQueryResult result = apixSearchService.query(query);

        Assert.assertEquals(1, result.totalResultCount);
        Assert.assertEquals(nodeAllowed.toString(), result.getNoderefs().get(0));
    }

    @Test
    public void testGetNodeMetadata() {
        // Switch to non-admin user
        AuthenticationUtil.setFullyAuthenticatedUser(USERNAME_NORIGHTS_JOS);

        // Allowed case
        NodeMetadata result = apixNodeService.getMetadata(new eu.xenit.alfred.api.data.NodeRef(nodeAllowed.toString()));
        Assert.assertFalse(result.getProperties().isEmpty());

        // Forbidden case
        try {
            apixNodeService.getMetadata(new eu.xenit.alfred.api.data.NodeRef(nodeForbidden.toString()));
            Assert.fail("Expected AccessDeniedException");
        } catch (AccessDeniedException e) {
        }
    }

}
