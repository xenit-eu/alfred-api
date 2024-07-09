package eu.xenit.apix.tests;

import com.github.ruediste.remoteJUnit.client.RemoteTestRunner;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.server.ApplicationContextProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;


@RunWith(RemoteTestRunner.class)
//TODO - check if the apic-impl:apix-.. lib needs to be imported like this.
// UseSpringContextOfBundle got commented out
// * `@UseSpringContextOfBundle`: Runs tests with the application context of a different bundle. By default, tests will be run in the context of the test bundle.
//@UseSpringContextOfBundle(filter = ApixImplBundleFilter.class)
// Potential solution, add the HElper classes into the runtimeClassPath so we can actually call upon them for when we start the integration tests
public abstract class BaseTest {

    //Apix Test model contstants
    public static final String APIX_TESTCM_NAMESPACE = "http://test.apix.xenit.eu/model/content";
    public static final String APIX_TESTCM_PREFIX = "apixtest";
    public static final String APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_SHORTNAME = "searchServiceLimitTestProperty";
    public static final String APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_PREFIXED =
            APIX_TESTCM_PREFIX + ":" + APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_SHORTNAME;

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private static final String mainTestFolderName = "ApixMainTestFolder";

    protected ApixToAlfrescoConversion c;
    protected ServiceRegistry serviceRegistry;
    protected Repository repository;
    private ApplicationContext testApplicationContext;

    protected void initialiseBeans () {
        logger.error("TO DELETE - initialiseBeans BaseTest");
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        serviceRegistry = testApplicationContext.getBean(ServiceRegistry.class);
        c =  testApplicationContext.getBean(ApixToAlfrescoConversion.class);
        repository = testApplicationContext.getBean(Repository.class);
    }

    protected NodeRef getNodeAtPath(String path) {
        if("/app:company_home".equals(path)) {
            return repository.getCompanyHome();
        }
        SearchService searchService = serviceRegistry.getSearchService();
        StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, path);
        NodeRef companyHomeNodeRef = null;
        try {
            if (resultSet.length() == 0) {
                throw new RuntimeException("Didn't find node at: " + path);
            }
            companyHomeNodeRef = resultSet.getNodeRef(0);
        } finally {
            resultSet.close();
        }
        return companyHomeNodeRef;
    }

    protected NodeRef getMainTestFolder() {
        NodeService nodeService = serviceRegistry.getNodeService();
        return nodeService.getChildByName(repository.getCompanyHome(), ContentModel.ASSOC_CONTAINS, mainTestFolderName);
    }

    protected FileInfo createMainTestFolder(NodeRef parentRef) {
        return createTestFolder(parentRef, mainTestFolderName);
    }

    protected FileInfo createTestFolder(NodeRef parentRef, String name) {
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();

        FileInfo testFolder = fileFolderService.create(parentRef, name, ContentModel.TYPE_FOLDER);
        alfrescoNodeService.addAspect(testFolder.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
        return testFolder;
    }

    protected FileInfo createTestNode(NodeRef parentRef, String name) {
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();
        FileInfo testNode = fileFolderService.create(parentRef, name, ContentModel.TYPE_CONTENT);
        logger.error("newFile {}" , testNode);

        alfrescoNodeService.addAspect(testNode.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
        alfrescoNodeService.addAspect(testNode.getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);
        return testNode;
    }

    protected boolean removeTestNode(NodeRef nodeRef) {
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();
        boolean success = false;
        if (alfrescoNodeService.exists(nodeRef)) {
            alfrescoNodeService.deleteNode(nodeRef);
            success = true;
        }
        return success;
    }

    protected boolean removeTestPersonNode(NodeRef nodeRef) {
        NodeService alfrescoNodeService = serviceRegistry.getNodeService();
        boolean success = false;
        if (alfrescoNodeService.exists(nodeRef)) {
            alfrescoNodeService.deleteNode(nodeRef);
            success = true;
        }
        return success;
    }

    protected File createTextFileWithContent(String fileName, String content) throws IOException {
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        File textFile = new File(fileName);
        textFile.deleteOnExit();
        textFile.createNewFile();
        try(PrintWriter writer = new PrintWriter(fileName, "UTF-8")){
            writer.println(content);
        }
        return textFile;
    }

    protected boolean cleanUp() {
        NodeRef mainTestFolder = getMainTestFolder();
        if (mainTestFolder != null) {
            removeTestNode(mainTestFolder);
            logger.debug("Removing " + mainTestFolderName);
        }
        return true;
    }
}
