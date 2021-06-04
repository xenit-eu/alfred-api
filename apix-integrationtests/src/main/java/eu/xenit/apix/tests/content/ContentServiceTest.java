package eu.xenit.apix.tests.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.content.IContentService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.util.SolrTestHelper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


public class ContentServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(ContentServiceTest.class);
    @Autowired
    INodeService nodeService;
    @Autowired
    IContentService contentService;
    @Autowired
    ServiceRegistry serviceRegistry;

    //Test variables
    private NodeRef testNode;
    private FileInfo mainTestFolder;

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

    public void Setup() {
        this.cleanUp();
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
    }

    public NodeRef createContentNode(NodeRef parent, String name, String text) {
        return createContentNodeRefS(parent, name, text, this.serviceRegistry);
    }

    @Test
    public void TestContentUrlExists() {
        Setup();
        try {
            solrHelper.waitForSolrSync();
        } catch (InterruptedException e) {
            Assert.fail(String.format("Interupted while awaiting solr synced state. Exception: %s", e));
        }
        NodeService alfNodeService = serviceRegistry.getNodeService();
        testNode = createContentNode(mainTestFolder.getNodeRef(), "testnode", "my content");
        ContentData d = (ContentData) alfNodeService.getProperty(testNode, ContentModel.PROP_CONTENT);
        boolean shouldExistCorrect = contentService.contentUrlExists(d.getContentUrl());
        boolean shouldExistIncorrect = contentService.contentUrlExists(d.getContentUrl() + "1");
        assertTrue(shouldExistCorrect);
        assertFalse(shouldExistIncorrect);
    }
}


