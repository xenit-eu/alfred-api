package eu.xenit.apix.rest.v1.tests;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.ChildParentAssociation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.rest.v1.nodes.CreateNodeOptions;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;

/**
 * Created by kenneth on 17.03.16.
 */
public class CopyNodeTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(CopyNodeTest.class);
    private NodeRef parentRef;
    private NodeRef copyFromFile;


    @Autowired
    INodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Autowired
    private ApixToAlfrescoConversion c;


    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        parentRef = c.apix(getMainTestFolder());
        copyFromFile = initializedNodeRefs.get(BaseTest.TESTFILE_NAME);

    }


    @Test
    public void TestDeserializeNodeInfoJson() throws IOException {
        NodeInfo nodeinfo = new NodeInfo();
        nodeinfo.setNoderef(parentRef);
        nodeinfo.associations = new NodeAssociations();
        nodeinfo.associations.setParents(new ArrayList<ChildParentAssociation>());
        nodeinfo.associations.getParents().add(new ChildParentAssociation(new NodeRef("workspace://SpacesStore/7987"),
                new NodeRef("workspace://SpacesStore/7987"), new QName("hello"), false));
        QName qName = new QName(ContentModel.TYPE_CONTENT.toString());
        Map<QName,List<String>> properties = new HashMap<>();
        List<String> title = new ArrayList<>();
        title.add("Title");
        properties.put(c.apix(ContentModel.PROP_TITLE), title);
        List<QName> qNames = new ArrayList<>();
        qNames.add(c.apix(ContentModel.ASPECT_AUDITABLE));
        NodeMetadata nodeMetadata = new NodeMetadata(parentRef, qName, qName, 0, properties, qNames);
        nodeinfo.setMetadata(nodeMetadata);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

        System.out.println(new ObjectMapper().writeValueAsString(nodeinfo));
        objectMapper.readValue(objectMapper.writeValueAsString(nodeinfo), NodeInfo.class);
    }

    @Test
    public void TestDeserializeNodeMetadata() throws IOException {
        QName qName = new QName(ContentModel.TYPE_CONTENT.toString());
        Map<QName,List<String>> properties = new HashMap<>();
        List<String> title = new ArrayList<>();
        title.add("Title");
        properties.put(c.apix(ContentModel.PROP_TITLE), title);
        List<QName> qNames = new ArrayList<>();
        qNames.add(c.apix(ContentModel.ASPECT_AUDITABLE));
        NodeMetadata nodeMetadata = new NodeMetadata(parentRef, qName, qName, 0, properties, qNames);
        System.out.println(new ObjectMapper().writeValueAsString(nodeMetadata));
        new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(nodeMetadata), NodeMetadata.class);

    }

    @Test
    public void TestDeserializeNodeMetadataFromTestNode() throws IOException {
        NodeMetadata nodeMetadata = nodeService.getMetadata(copyFromFile);
        new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(nodeMetadata), NodeMetadata.class);

    }



    @Test
    public void testCopyFileNode() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentRef, null, null, null , copyFromFile);
        NodeInfo createdRefInfo = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_OK);
                    return null;
                }, false, true);

        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
        assertEquals(2, newChildAssocs.size());
    }

    @Test
    public void testCopyNodeWithName() throws Throwable {
        final String newName = "NewName";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentRef, newName, null, null, copyFromFile);
        NodeInfo createdRefInfo = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_OK);
                    return null;
                }, false, true);

        assertEquals(true, nodeService.exists(createdRefInfo.noderef));
        assertEquals(newName, createdRefInfo.metadata.properties.get(ContentModel.PROP_NAME.toString()));
        assertEquals(newName, nodeService.getMetadata(createdRefInfo.noderef).properties.get(ContentModel.PROP_NAME.toString()));

//        JSONObject responseProperties = (JSONObject) ((JSONObject) response.get("metadata")).get("properties");
//        JSONArray responseNameProperty = (JSONArray) responseProperties.get();
//        JSONArray responseTitleProperty = (JSONArray) responseProperties.get(ContentModel.PROP_TITLE.toString());
//        assertEquals(true, nodeService.exists(new NodeRef(newRef)));
//        assertEquals(newName, (String) responseNameProperty.get(0));

    }

    @Test
    public void testCopyNodeWithProperties() throws Throwable {
        final String newName = "NewName1";
        final String newTitle = "NewTitle1";
        HashMap<QName, String[]> properties = new HashMap<>();
        properties.put(new QName(ContentModel.PROP_TITLE.toString()) , new String[]{newTitle});

        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentRef, newName, null, null, copyFromFile);
        NodeInfo createdRefInfo = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_OK);
                    return null;
                }, false, true);

        assertEquals(true, nodeService.exists(createdRefInfo.noderef));
        assertEquals(newName, createdRefInfo.metadata.properties.get(ContentModel.PROP_NAME.toString()));
        assertEquals(newName, nodeService.getMetadata(createdRefInfo.noderef).properties.get(ContentModel.PROP_NAME.toString()));
        assertEquals(newName, createdRefInfo.metadata.properties.get(ContentModel.PROP_TITLE.toString()));
        assertEquals(newName, nodeService.getMetadata(createdRefInfo.noderef).properties.get(ContentModel.PROP_TITLE.toString()));

//        JSONObject responseProperties = (JSONObject) ((JSONObject) response.get("metadata")).get("properties");
//        JSONArray responseNameProperty = (JSONArray) responseProperties.get(ContentModel.PROP_NAME.toString());
//        JSONArray responseTitleProperty = (JSONArray) responseProperties.get(ContentModel.PROP_TITLE.toString());
//        assertEquals(true, nodeService.exists(new NodeRef(newRef)));
//        assertEquals(newName, (String) responseNameProperty.get(0));
//        assertEquals(newTitle, (String) responseTitleProperty.get(0));

    }
//
//    @Test
//    public void copyNodeReturnsAccesDenied() {
//        final HashMap<String, NodeRef> initializedNodeRefs = init();
//        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME));
//        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
//        final NodeRef parentRef = primaryParentAssoc.getTarget();
//
//        final String url = makeAlfrescoBaseurl(BaseTest.USERWITHOUTRIGHTS, BaseTest.USERWITHOUTRIGHTS) + "/apix/v1/nodes";
//        final CloseableHttpClient httpclient = HttpClients.createDefault();
//
//        transactionService.getRetryingTransactionHelper()
//                .doInTransaction(() -> {
//                    doTestCopy(httpclient, url, parentRef, initializedNodeRefs.get(BaseTest.NOUSERRIGHTS_FILE_NAME).toString(), null, null, 403);
//                    return null;
//                }, false, true);
//
//        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parentRef);
//        assertEquals(1, newChildAssocs.size());
//    }


    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
