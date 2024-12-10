package eu.xenit.alfred.api.tests.search;

import static org.alfresco.repo.version.VersionModel.PROP_QNAME_VERSION_LABEL;

import eu.xenit.alfred.api.search.ISearchService;
import eu.xenit.alfred.api.search.QueryBuilder;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.SearchQueryResult;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;

import org.alfresco.service.namespace.QName;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
    private ISearchService searchService;

    // Alfresco services
    private NodeService alfNodeService;
    private PermissionService alfPermissionService;
    private AuthorityService alfAuthorityService;
    private PersonService alfPersonService;
    private MutableAuthenticationService alfAuthenticationService;

    public PermissionsTest() {
        searchService = getBean(ISearchService.class);
        alfNodeService = getBean("NodeService", NodeService.class);
        alfPermissionService = getBean("PermissionService", PermissionService.class);
        alfAuthorityService = getBean("AuthorityService", AuthorityService.class);
        alfAuthenticationService = getBean("AuthenticationService", MutableAuthenticationService.class);
        alfPersonService = getBean("PersonService", PersonService.class);
    }

    @BeforeClass
    public void setupSuite() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        try {
            createMainTestFolder(repository.getCompanyHome());
        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }
        //permissionService.setInheritParentPermissions(getMainTestFolder(), false);

        // Add users and groups
        createUserAndGroupsWithoutRights();

        // Set up folders with group permissions
        try {
            FileInfo folderForbidden = createTestFolder(getMainTestFolder(), "Forbidden");
            alfPermissionService.setInheritParentPermissions(folderForbidden.getNodeRef(), false);
            FileInfo info = createTestNode(folderForbidden.getNodeRef(), "ForbiddenDocument");
            alfNodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }

        try {
            FileInfo folderAllowed = createTestFolder(getMainTestFolder(), "Allowed");
            alfPermissionService.setInheritParentPermissions(folderAllowed.getNodeRef(), false);
            alfPermissionService.setPermission(
                    folderAllowed.getNodeRef(), GROUPID, PermissionService.COORDINATOR, true);
            FileInfo info = createTestNode(folderAllowed.getNodeRef(), "AllowedDocument");
            alfNodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }
    }

    @AfterClass
    public void tearDownSuite() {
        //AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        //personService.deletePerson(USERNAME_JOS);
        //cleanUp();
    }

    private void createUserAndGroupsWithoutRights() {
        try {
            alfAuthenticationService.createAuthentication(USERNAME_NORIGHTS_JOS, "foobar".toCharArray());
            Map<QName, Serializable> userProperties = new HashMap<>();
            userProperties.put(ContentModel.PROP_USERNAME, USERNAME_NORIGHTS_JOS);
            userProperties.put(ContentModel.PROP_FIRSTNAME, "Jos");
            userProperties.put(ContentModel.PROP_LASTNAME, "NoRights");
            userProperties.put(ContentModel.PROP_EMAIL, "nojosno@example.com");
            alfAuthenticationService.getAuthenticationEnabled(USERNAME_NORIGHTS_JOS);
            alfPersonService.createPerson(userProperties);

            alfAuthorityService.createAuthority(AuthorityType.GROUP, GROUPNAME);
            alfAuthorityService.addAuthority(GROUPID, USERNAME_NORIGHTS_JOS);
        } catch (AuthenticationException e) {
            // User and groups were already created. Skip.
            logger.warn("User and groups already created. Skipping (" + e.getMessage() + ")");
        }
    }

    @Test
    public void testSearch() {
        logger.error("WIM: Search SANITY"); // REMOVE ME

        // Add users and groups
        createUserAndGroupsWithoutRights();

        // Set up folders with group permissions
        try {
            FileInfo folderForbidden = createTestFolder(getMainTestFolder(), "Forbidden");
            alfPermissionService.setInheritParentPermissions(folderForbidden.getNodeRef(), false);
            FileInfo info = createTestNode(folderForbidden.getNodeRef(), "ForbiddenDocument");
            alfNodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }

        try {
            FileInfo folderAllowed = createTestFolder(getMainTestFolder(), "Allowed");
            alfPermissionService.setInheritParentPermissions(folderAllowed.getNodeRef(), false);
            alfPermissionService.setPermission(
                    folderAllowed.getNodeRef(), GROUPID, PermissionService.COORDINATOR, true);
            FileInfo info = createTestNode(folderAllowed.getNodeRef(), "AllowedDocument");
            alfNodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }

        // Switch to user without rights
        AuthenticationUtil.setFullyAuthenticatedUser(USERNAME_NORIGHTS_JOS);
        logger.error("WIM: auth f user: " + AuthenticationUtil.getFullyAuthenticatedUser()); //// REMOVEME

        // Perform search
        SearchSyntaxNode queryNode = new QueryBuilder()
                .startAnd()
                .term("path", "/app:company_home/cm:" + mainTestFolderName + "//*") // x2 slash means: recurse
                .term("type", "cm:content")
                .end()
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(queryNode);
        SearchQueryResult result = searchService.query(query);

        logger.error("WIM: r:: " + result); //// REMOVEME
        Assert.assertEquals(1, result.totalResultCount);

        logger.error("WIM: Your father would be proud, Fox"); // REMOVE ME
    }


    @Test
    public void testNode() {
        logger.error("WIM: Node SANITY"); // REMOVE ME

        // Switch to user without rights
        AuthenticationUtil.setFullyAuthenticatedUser(USERNAME_NORIGHTS_JOS);
        logger.error("WIM: auth f user: " + AuthenticationUtil.getFullyAuthenticatedUser()); //// REMOVEME

        // Perform search
        SearchSyntaxNode queryNode = new QueryBuilder()
                .startAnd()
                .term("path", "/app:company_home/cm:" + mainTestFolderName + "//*") // x2 slash means: recurse
                .term("type", "cm:content")
                .end()
                .create();

        SearchQuery query = new SearchQuery();
        query.setQuery(queryNode);
        SearchQueryResult result = searchService.query(query);

        logger.error("WIM: r:: " + result); //// REMOVEME
        Assert.assertEquals(1, result.totalResultCount);

        logger.error("WIM: Your father would be proud, Fox"); // REMOVE ME
    }


}
