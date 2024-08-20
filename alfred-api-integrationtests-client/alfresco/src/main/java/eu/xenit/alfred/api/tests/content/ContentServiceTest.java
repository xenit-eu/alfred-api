package eu.xenit.alfred.api.tests.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.alfred.api.content.IContentService;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import eu.xenit.alfred.api.util.SolrTestHelperImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContentServiceTest extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(ContentServiceTest.class);

    INodeService nodeService;
    IContentService contentService;
    SolrTestHelperImpl solrHelper;
    //Test variables
    private NodeRef testNode;
    private FileInfo mainTestFolder;

    public ContentServiceTest() {
        // initialise the local beans
        contentService = getBean(IContentService.class);
        nodeService = getBean(INodeService.class);
        solrHelper = getBean(SolrTestHelperImpl.class);
    }

    @Before
    public void setupContentServiceTest() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        this.cleanUp();
        try {
            solrHelper.waitForTransactionSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
    }

    public static NodeRef createContentNodeRefS(NodeRef parent, String name, String text,
            ServiceRegistry serviceRegistry) {
        ContentService alfcontentService = serviceRegistry.getContentService();
        // Create a map to contain the values of the properties of the node
        NodeService ns = serviceRegistry.getNodeService();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);

        // use the node service to create a new node
        NodeRef node = ns.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();

        // Use the content service to set the content onto the newly created node
        ContentWriter writer = alfcontentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(text);

        // Return a node reference to the newly created node
        return node;
    }


    public NodeRef createContentNode(NodeRef parent, String name, String text) {
        return createContentNodeRefS(parent, name, text, this.serviceRegistry);
    }

    @Test
    public void TestContentUrlExists() {
        NodeService alfNodeService = serviceRegistry.getNodeService();
        testNode = createContentNode(mainTestFolder.getNodeRef(), "testnode", "my content");
        ContentData d = (ContentData) alfNodeService.getProperty(testNode, ContentModel.PROP_CONTENT);
        boolean shouldExistCorrect = contentService.contentUrlExists(d.getContentUrl());
        boolean shouldExistIncorrect = contentService.contentUrlExists(d.getContentUrl() + "1");
        assertTrue(shouldExistCorrect);
        assertFalse(shouldExistIncorrect);
    }
}


