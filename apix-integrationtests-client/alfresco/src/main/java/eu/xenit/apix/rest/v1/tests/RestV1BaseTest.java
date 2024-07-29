package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.BaseApplicationContextTest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Created by kenneth on 14.03.16.
 */
public abstract class RestV1BaseTest extends BaseApplicationContextTest {

    private final static Logger logger = LoggerFactory.getLogger(RestV1BaseTest.class);
    private final static String VERSION = "v1";

    public static final String MAIN_TESTFOLDER_NAME = "mainTestFolder";
    public static final String TESTFOLDER_NAME = "testFolder";
    public static final String TESTFILE_NAME = "testFile";
    public static final String TESTFOLDER2_NAME = "testFolder2";
    public static final String TESTFILE2_NAME = "testFile2";
    public static final String TESTFILE3_NAME = "testFile3";
    public static final String NOUSERRIGHTS_FOLDER_NAME = "noUserRightsTestFolder";
    public static final String NOUSERRIGHTS_FILE_NAME = "noUserRightsTestFile";

    public static final String USERWITHOUTRIGHTS = "red";
    public static final String USERWITHOUTRIGHTS_EMAIL =
            USERWITHOUTRIGHTS + "@" + USERWITHOUTRIGHTS + ".com";

    // This is a method so it can be overrided in v2
    // It's not static like the string because you can't override static methods :(
    protected String getVersion() {
        return VERSION;
    }

    protected String createApixUrl(String subUrl, Object... args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = URLEncoder.encodeUriComponent(args[i].toString());
        }
        return makeAlfrescoBaseurlAdmin() + "/apix/" + getVersion() + String.format(subUrl, args);
    }

    protected String makeAlfrescoBaseurlAdmin() {
        return makeAlfrescoBaseurl("admin", "admin");
    }

    protected String makeAlfrescoBaseurl(String userName, String passWord) {

        String protocol = "http";  // sysAdminParams.getAlfrescoProtocol();
        String host = "localhost"; // sysAdminParams.getAlfrescoHost();
        String port = "8080"; //  Integer.toString(sysAdminParams.getAlfrescoPort());

        //String protocol = sysAdminParams.getAlfrescoProtocol();
        //String host = sysAdminParams.getAlfrescoHost();
        //String port = Integer.toString(sysAdminParams.getAlfrescoPort());

        String base = protocol + "://" + userName + ":" + passWord + "@" + host + ":" + port + "/alfresco/s";
        return base;
    }

    protected String makeAlfrescoBaseurl() {
        String base = sysAdminParams.getAlfrescoProtocol() + "://"
                + sysAdminParams.getAlfrescoHost()
                + ":" + sysAdminParams.getAlfrescoPort()
                + "/alfresco/s";

        return base;
    }

    protected String makeNodesUrl(String space, String store, String guid, String action, String userName,
            String passWord) {
        return String.format(makeAlfrescoBaseurl(userName, passWord) + "/apix/%s/nodes/%s/%s/%s%s", getVersion(), space,
                store, guid, action);
    }

    protected String makeFormsUrl(String space, String store, String guid, String action, String userName,
            String passWord) {
        return String
                .format(makeAlfrescoBaseurl(userName, passWord) + "/apix/%s/temp/forms/%s/%s/%s%s", getVersion(), space,
                        store, guid, action);
    }

    protected String makeFormsUrl(eu.xenit.apix.data.NodeRef nodeRef, String action, String userName, String passWord) {
        String space = nodeRef.getStoreRefProtocol();
        String store = nodeRef.getStoreRefId();
        String guid = nodeRef.getGuid();

        return this.makeFormsUrl(space, store, guid, action, userName, passWord);
    }

    protected String makeFormsUrl(eu.xenit.apix.data.NodeRef nodeRef, String userName, String passWord) {
        return this.makeFormsUrl(nodeRef, "", userName, passWord);
    }

    protected String makeFormsUrl(eu.xenit.apix.data.NodeRef nodeRef) {
        return this.makeFormsUrl(nodeRef, "admin", "admin");
    }

    protected String makeWorkingCopiesUrl(String space, String store, String guid, String action, String userName,
            String passWord) {
        return String
                .format(makeAlfrescoBaseurl(userName, passWord) + "/apix/%s/workingcopies/%s/%s/%s%s", getVersion(),
                        space, store, guid, action);
    }

    protected String makeWorkingCopiesUrl(eu.xenit.apix.data.NodeRef nodeRef, String action, String userName,
            String passWord) {
        String space = nodeRef.getStoreRefProtocol();
        String store = nodeRef.getStoreRefId();
        String guid = nodeRef.getGuid();

        return this.makeWorkingCopiesUrl(space, store, guid, action, userName, passWord);
    }

    protected String makeWorkingCopiesUrl(String space, String store, String guid, String userName, String passWord) {
        return this.makeWorkingCopiesUrl(space, store, guid, "", userName, passWord);
    }

    protected String makeNodesUrlWithTicket(String space, String store, String guid, String action) {
        String ticket = authenticationService.getCurrentTicket();
        return String
                .format(makeAlfrescoBaseurl() + "/apix/%s/nodes/%s/%s/%s%s?alf_ticket=%s", getVersion(), space, store,
                        guid, action, ticket);
    }

    protected String makeBulkUrl() {
        return String.format(makeAlfrescoBaseurl("admin", "admin") + "/apix/" + getVersion() + "/bulk");
    }

    protected String makeBulkUrlWithTicket() {
        String ticket = authenticationService.getCurrentTicket();
        return String.format(makeAlfrescoBaseurl() + "/apix/" + getVersion() + "/bulk?alf_ticket=" + ticket);
    }

    protected String makeCommentsUrl(String space, String store, String guid, String userName,
            String password) {
        return String.format(makeAlfrescoBaseurl(userName, password) + "/apix/%s/comments/%s/%s/%s", getVersion(),
                space, store, guid);
    }

    protected String makeCommentsUrl(eu.xenit.apix.data.NodeRef nodeRef, String userName, String password) {
        String space = nodeRef.getStoreRefProtocol();
        String store = nodeRef.getStoreRefId();
        String guid = nodeRef.getGuid();
        return this.makeCommentsUrl(space, store, guid, userName, password);
    }

    protected HashMap<String, eu.xenit.apix.data.NodeRef> init() {
        return init(null);
    }

    protected HashMap<String, eu.xenit.apix.data.NodeRef> init(final String testName) {
        final HashMap<String, eu.xenit.apix.data.NodeRef> initializedNodeRefs = new HashMap<>();

        TransactionService transactionService = serviceRegistry.getTransactionService();
        this.removeMainTestFolder();

        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = () -> {
            String mainTestFolderName = MAIN_TESTFOLDER_NAME + (testName != null ? "_" + testName : "");
            NodeRef companyHomeRef = repository.getCompanyHome();
            FileInfo mainTestFolder = createTestNode(companyHomeRef, mainTestFolderName,
                    ContentModel.TYPE_FOLDER);
            FileInfo testFolder = createTestNode(mainTestFolder.getNodeRef(), TESTFOLDER_NAME,
                    ContentModel.TYPE_FOLDER);
            initializedNodeRefs.put(TESTFOLDER_NAME,
                    new eu.xenit.apix.data.NodeRef(testFolder.getNodeRef().toString()));
            FileInfo testNode = createTestNode(testFolder.getNodeRef(), TESTFILE_NAME, ContentModel.TYPE_CONTENT);
            NodeRef testNodeRef = testNode.getNodeRef();
            eu.xenit.apix.data.NodeRef apixTestNodeRef = new eu.xenit.apix.data.NodeRef(testNodeRef.toString());
            initializedNodeRefs.put(TESTFILE_NAME, apixTestNodeRef);

            FileInfo testFolder2 = createTestNode(mainTestFolder.getNodeRef(), TESTFOLDER2_NAME,
                    ContentModel.TYPE_FOLDER);
            FileInfo testNode2 = createTestNode(testFolder2.getNodeRef(), TESTFILE2_NAME,
                    ContentModel.TYPE_CONTENT);
            NodeRef testNodeRef2 = testNode2.getNodeRef();
            eu.xenit.apix.data.NodeRef apixTestNodeRef2 = new eu.xenit.apix.data.NodeRef(testNodeRef2.toString());
            initializedNodeRefs.put(TESTFILE2_NAME, apixTestNodeRef2);

            FileInfo testNode3 = createTestNode(testFolder2.getNodeRef(), TESTFILE3_NAME,
                    ContentModel.TYPE_CONTENT);
            NodeRef testNodeRef3 = testNode3.getNodeRef();
            eu.xenit.apix.data.NodeRef apixTestNodeRef3 = new eu.xenit.apix.data.NodeRef(testNodeRef3.toString());
            initializedNodeRefs.put(TESTFILE3_NAME, apixTestNodeRef3);

            FileInfo noUserRightsFolder = createTestNode(mainTestFolder.getNodeRef(), NOUSERRIGHTS_FOLDER_NAME,
                    ContentModel.TYPE_FOLDER);
            setPermissionInheritance(noUserRightsFolder.getNodeRef(), false);
            FileInfo noUserRightsNode = createTestNode(noUserRightsFolder.getNodeRef(), NOUSERRIGHTS_FILE_NAME,
                    ContentModel.TYPE_CONTENT);
            NodeRef noUserRightsNodeRef = noUserRightsNode.getNodeRef();
            setPermissionInheritance(noUserRightsNodeRef, false);
            eu.xenit.apix.data.NodeRef apixNoUserRightsNodeRef = new eu.xenit.apix.data.NodeRef(
                    noUserRightsNodeRef.toString());
            initializedNodeRefs.put(NOUSERRIGHTS_FILE_NAME, apixNoUserRightsNodeRef);

            createUser(USERWITHOUTRIGHTS, USERWITHOUTRIGHTS, USERWITHOUTRIGHTS,
                    USERWITHOUTRIGHTS_EMAIL);
            return null;
        };

        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
        return initializedNodeRefs;
    }

    protected String makeNodesUrl(eu.xenit.apix.data.NodeRef nodeRef, String userName, String passWord) {
        return this.makeNodesUrl(nodeRef, "", userName, passWord);
    }

    protected String makeNodesUrlWithTicket(eu.xenit.apix.data.NodeRef nodeRef) {
        return this.makeNodesUrlWithTicket(nodeRef, "");
    }

    protected String makeNodesUrl(eu.xenit.apix.data.NodeRef nodeRef, String action, String userName, String passWord) {
        String space = nodeRef.getStoreRefProtocol();
        String store = nodeRef.getStoreRefId();
        String guid = nodeRef.getGuid();
        return this.makeNodesUrl(space, store, guid, action, userName, passWord);
    }

    protected String makeNodesUrlWithTicket(eu.xenit.apix.data.NodeRef nodeRef, String action) {
        String space = nodeRef.getStoreRefProtocol();
        String store = nodeRef.getStoreRefId();
        String guid = nodeRef.getGuid();
        return this.makeNodesUrlWithTicket(space, store, guid, action);
    }

    protected String makeNodesUrl(String guid, String action, String userName, String passWord) {
        String space = "workspace";
        String store = "SpacesStore";

        return this.makeNodesUrl(space, store, guid, action, userName, passWord);
    }

    protected String makeNodesUrlWithTicket(String guid, String action) {
        String space = "workspace";
        String store = "SpacesStore";

        return this.makeNodesUrlWithTicket(space, store, guid, action);
    }

    protected NodeRef getMainTestFolder() {
        FileFolderService fileFolderService = this.serviceRegistry.getFileFolderService();
        NodeRef nodeRef = fileFolderService.searchSimple(repository.getCompanyHome(), MAIN_TESTFOLDER_NAME);
        return nodeRef;
    }

    protected FileInfo createTestNode(NodeRef parentRef, String name, QName type) {
        FileFolderService fileFolderService = this.serviceRegistry.getFileFolderService();

        FileInfo testNode = fileFolderService.create(parentRef, name, type);
        return testNode;
    }

    protected boolean removeTestNode(NodeRef nodeRef) {
        NodeService alfrescoNodeService = this.serviceRegistry.getNodeService();
        CheckOutCheckInService checkOutCheckInService = this.serviceRegistry.getCheckOutCheckInService();
        boolean success = false;

        if (alfrescoNodeService.exists(nodeRef)) {
            List<NodeRef> childRefs = new ArrayList<>();
            this.getChildrenRecursive(nodeRef, childRefs);
            for (NodeRef childRef : childRefs) {
                if (checkOutCheckInService.isWorkingCopy(childRef)) {
                    checkOutCheckInService.cancelCheckout(childRef);
                }
            }

            alfrescoNodeService.deleteNode(nodeRef);
            success = true;
        }

        return success;
    }

    private void getChildrenRecursive(NodeRef nodeRef, List<NodeRef> childRefs) {
        NodeService alfrescoNodeService = this.serviceRegistry.getNodeService();

        List<ChildAssociationRef> childAssocs = alfrescoNodeService.getChildAssocs(nodeRef);
        if (childAssocs.size() > 0) {
            for (ChildAssociationRef childAssoc : childAssocs) {
                NodeRef childRef = childAssoc.getChildRef();
                this.getChildrenRecursive(childRef, childRefs);
            }
        } else {
            childRefs.add(nodeRef);
        }
    }

    protected void removeMainTestFolder() {
        RetryingTransactionHelper.RetryingTransactionCallback<Object> txnWork = new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            public Object execute() throws Exception {
                try {
                    NodeRef nodeRef = getMainTestFolder();
                    removeTestNode(nodeRef);
                } catch (RuntimeException ex) {
                    logger.debug("Did not need to remove mainTestFolder because it did not exist");
                    //ex.printStackTrace();
                }
                return null;
            }
        };
        TransactionService transactionService = serviceRegistry.getTransactionService();
        transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
    }

    protected void setPermissionInheritance(NodeRef target, boolean bool) {
        serviceRegistry.getPermissionService().setInheritParentPermissions(target, bool);
    }

    protected NodeRef createUser(String userName, String password, String firstName, String email) {
        PersonService personService = serviceRegistry.getPersonService();
        if (!personService.personExists(userName)) {
            Map<QName, Serializable> personProperties = new HashMap<>();
            personProperties.put(ContentModel.PROP_USERNAME, userName);
            personProperties.put(ContentModel.PROP_FIRSTNAME, firstName);
            personProperties.put(ContentModel.PROP_EMAIL, email);
            NodeRef personNodeRef = personService.createPerson(personProperties);
            serviceRegistry.getAuthenticationService().createAuthentication(userName, password.toCharArray());
            return personNodeRef;
        } else {
            return personService.getPerson(userName);
        }
    }

    public String json(String str) {
        return str.replaceAll("'", "\"");
    }

    public <T> T doPost(String checkoutUrl, Class<T> returnType, String jsonBody, Object... args) throws IOException {
        return doWithBody(new HttpPost(checkoutUrl), returnType, HttpStatus.SC_OK, jsonBody, args);
    }

    public <T> T doPostExpected(String checkoutUrl, Class<T> returnType, String jsonBody, int expectedResponse,
            Object... args) throws IOException {
        return doWithBody(new HttpPost(checkoutUrl), returnType, expectedResponse, jsonBody, args);
    }

    public <T> T doPut(String checkoutUrl, Class<T> returnType, String jsonBody, Object... args) throws IOException {
        return doWithBody(new HttpPut(checkoutUrl), returnType, HttpStatus.SC_OK, jsonBody, args);
    }

    public <T> T doPutExpected(String checkoutUrl, Class<T> returnType, String jsonBody, int expectedResponse,
            Object... args) throws IOException {
        return doWithBody(new HttpPut(checkoutUrl), returnType, expectedResponse, jsonBody, args);
    }

    private <T> T doWithBody(HttpEntityEnclosingRequestBase req, Class<T> returnType, int expectedResponseCode,
            String jsonBody, Object... args)
            throws IOException {
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        if (jsonBody != null) {
            String checkoutJsonString = json(String.format(jsonBody, args));
            req.setEntity(new StringEntity(checkoutJsonString, ContentType.APPLICATION_JSON));
        }

        try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
            byte[] result = EntityUtils.toByteArray(response.getEntity());
            assertEquals(expectedResponseCode, response.getStatusLine().getStatusCode());
            if (returnType == null) {
                return null;
            }

            try {
                T ret = new ObjectMapper().readValue(result, returnType);
                return ret;
            } catch (JsonMappingException | JsonParseException jsonException) {
                logger.error("Jackson was not able to automatically deserialise: " + returnType);
            }
            return null;
        }
    }

    public <T> T doGet(String checkoutUrl, Class<T> returnType) throws IOException {
        final HttpResponse response = Request.Get(checkoutUrl).execute().returnResponse();
        String result = EntityUtils.toString(response.getEntity());

        assertEquals(200, response.getStatusLine().getStatusCode());
        if (returnType == null) {
            return null;
        }
        T ret = new ObjectMapper().readValue(result, returnType);
        return ret;
    }

    /*public <T> T doDelete(String checkoutUrl, Class<T> returnType) throws IOException {
        return doDelete(checkoutUrl, returnType, null);
    }*/

    public <T> T doDelete(String url, Class<T> returnType) throws IOException {
        //String jsonString = json(String.format(jsonBody, args));
        Request delete = Request.Delete(url);
        /*if (jsonBody != null)
            delete = delete.bodyString(jsonString, ContentType.APPLICATION_JSON);*/
        final HttpResponse response = delete.execute().returnResponse();

        String result = EntityUtils.toString(response.getEntity());

        assertEquals(200, response.getStatusLine().getStatusCode());

        if (returnType == null) {
            return null;
        }
        T ret = new ObjectMapper().readValue(result, returnType);
        return ret;
    }


}
