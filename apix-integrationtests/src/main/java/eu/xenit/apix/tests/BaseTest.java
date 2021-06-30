package eu.xenit.apix.tests;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.util.SolrTestHelper;
import eu.xenit.testing.integrationtesting.runner.AlfrescoTestRunner;
import eu.xenit.testing.integrationtesting.runner.UseSpringContextOfBundle;
import java.util.Properties;
import javax.sql.DataSource;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.beans.factory.annotation.Qualifier;


@RunWith(AlfrescoTestRunner.class)
@UseSpringContextOfBundle(filter = ApixImplBundleFilter.class)
public abstract class BaseTest implements InitializingBean {

    //Apix Test model contstants
    public final static String APIX_TESTCM_NAMESPACE = "http://test.apix.xenit.eu/model/content";
    public final static String APIX_TESTCM_PREFIX = "apixtest";
    public final static String APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_SHORTNAME = "searchServiceLimitTestProperty";
    public final static String APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_PREFIXED =
            APIX_TESTCM_PREFIX + ":" + APIX_TESTCM_PROP_SEARCHSERVICELIMITTEST_SHORTNAME;

    private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private final static String mainTestFolderName = "ApixMainTestFolder";

    @Autowired
    protected ApixToAlfrescoConversion c;
    @Autowired
    protected ServiceRegistry serviceRegistry;
    @Autowired
    protected Repository repository;
    @Autowired
    DataSource dataSource;
    @Autowired
    @Qualifier("Search")
    SwitchableApplicationContextFactory searchSubSystem;
    @Autowired
    @Qualifier("global-properties")
    Properties globalProperties;

    public SolrTestHelper solrHelper;

    public void afterPropertiesSet() {
        String subsystem = globalProperties.getProperty("index.subsystem.name");
        String solrBaseUrl = subsystem.equals("solr4") ? "/solr4" : "/solr";
        solrHelper = new SolrTestHelper(solrBaseUrl, dataSource, searchSubSystem);
    }

    protected NodeRef getNodeAtPath(String path) {
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
