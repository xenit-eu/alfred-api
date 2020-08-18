package eu.xenit.apix.rest.v1.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.data.NodeRef;
import java.util.HashMap;

import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeMetadata;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by kenneth on 14.03.16.
 */
public class MetadataTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(MetadataTest.class);

    @Autowired
    @Qualifier("FileFolderService")
    FileFolderService fileFolderService;

    @Autowired
    @Qualifier("NodeService")
    NodeService nodeService;

    @Autowired
    INodeService iNodeService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Autowired
    @Qualifier("AuthenticationService")
    AuthenticationService authenticationService;

    @Autowired
    NodeArchiveService nodeArchiveService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void ErrorTest() throws Exception {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        System.out.println("ErrorTest");
        throw new Exception("Test");
    }

    @Test
    public void TestDeserializeNodeMetaDataJson() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        NodeMetadata nodeMetadata = iNodeService.getMetadata(initializedNodeRefs.get(BaseTest.TESTFILE_NAME));
        ObjectMapper objectMapper = new ObjectMapper();
        String metadata = objectMapper.writeValueAsString(nodeMetadata);
        System.out.println("Metadata : " + metadata);
//        try{
//            objectMapper.readValue(metadata, NodeMetadata.class);
//        } catch (Exception exception) {
//            System.out.println(exception);
//        }
        objectMapper.readValue(metadata, NodeMetadata.class);
        System.out.println("End changed");
    }

    @Test
    public void ErrorTestAfter() throws Exception {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        System.out.println("ErrorTest");
        throw new Exception("Test");
    }
}
