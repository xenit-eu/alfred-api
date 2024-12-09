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
    private ISearchService searchService;

    // Alfresco services
    //private FileFolderService fileFolderService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;

    public PermissionsTest() {
        searchService = getBean(ISearchService.class);
        // fileFolderService = getBean(FileFolderService.class);
        nodeService = getBean("NodeService", NodeService.class);
        permissionService = getBean("PermissionService", PermissionService.class);
        authorityService = getBean("AuthorityService", AuthorityService.class);
        authenticationService = getBean("AuthenticationService", MutableAuthenticationService.class);
        personService = getBean("PersonService", PersonService.class);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        try {
            createMainTestFolder(repository.getCompanyHome());
        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }
        //permissionService.setInheritParentPermissions(getMainTestFolder(), false);
    }

    @After
    public void tearDown() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        //personService.deletePerson(USERNAME_JOS);
        //cleanUp();
    }

    private void createUserAndGroupsWithoutRights() {
        try {
            authenticationService.createAuthentication(USERNAME_NORIGHTS_JOS, "foobar".toCharArray());
            Map<QName, Serializable> userProperties = new HashMap<>();
            userProperties.put(ContentModel.PROP_USERNAME, USERNAME_NORIGHTS_JOS);
            userProperties.put(ContentModel.PROP_FIRSTNAME, "Jos");
            userProperties.put(ContentModel.PROP_LASTNAME, "NoRights");
            userProperties.put(ContentModel.PROP_EMAIL, "nojosno@example.com");
            authenticationService.getAuthenticationEnabled(USERNAME_NORIGHTS_JOS);
            personService.createPerson(userProperties);

            authorityService.createAuthority(AuthorityType.GROUP, GROUPNAME);
            authorityService.addAuthority(GROUPID, USERNAME_NORIGHTS_JOS);
        } catch (AuthenticationException e) {
            // User and groups were already created. Skip.
            logger.warn("User and groups already created. Skipping (" + e.getMessage() + ")");
        }
    }

    @Test
    public void test() {
        logger.error("WIM: SANITY"); // REMOVE ME

        // Add users and groups
        createUserAndGroupsWithoutRights();

        // Set up folders with group permissions
        try {
            FileInfo folderForbidden = createTestFolder(getMainTestFolder(), "Forbidden");
            permissionService.setInheritParentPermissions(folderForbidden.getNodeRef(), false);
            FileInfo info = createTestNode(folderForbidden.getNodeRef(), "ForbiddenDocument");
            nodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

        } catch (FileExistsException e) {
            logger.warn("Test folder already created. Skipping (" + e.getMessage() + ")");
        }

        try {
            FileInfo folderAllowed = createTestFolder(getMainTestFolder(), "Allowed");
            permissionService.setInheritParentPermissions(folderAllowed.getNodeRef(), false);
            permissionService.setPermission(
                    folderAllowed.getNodeRef(), GROUPID, PermissionService.COORDINATOR, true);
            FileInfo info = createTestNode(folderAllowed.getNodeRef(), "AllowedDocument");
            nodeService.setProperty(info.getNodeRef(), PROP_QNAME_VERSION_LABEL, PROPERTY_VALUE);

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


}
