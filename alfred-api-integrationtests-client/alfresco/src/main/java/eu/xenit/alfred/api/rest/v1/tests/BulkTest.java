package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.ChildParentAssociation;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.rest.RestCall;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kenneth on 18.03.16.
 */
public class BulkTest extends RestV1BaseTest {

    public static final String AUTHENTICATION_IN_URL = "alf_ticket=";
    private final static Logger logger = LoggerFactory.getLogger(BulkTest.class);
    private final INodeService nodeService;
    private final NodeService alfrescoNodeService;

    public BulkTest() {
        // initialise the local beans
        alfrescoNodeService = serviceRegistry.getNodeService();
        nodeService = getBean(eu.xenit.alfred.api.alfresco.metadata.NodeService.class); // fetches AlfredApi nodeService
    }

    private String jsonObjectGetStringFromInt(JSONObject targetObject, String key) {
        return String.valueOf(targetObject.getInt(key));
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetBulk() throws IOException, JSONException {
        HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());
        NodeRef parentRef = primaryParentAssoc.getTarget();

        String url = makeBulkUrl();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        String firstGetUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        String secondGetUrl = removePrefixAndAuthenticate(makeNodesUrl(parentRef, "/metadata", "admin", "admin"));
        StringEntity entity = new StringEntity(json(String
                .format("[{'url':'%s','method':'%s'},{'url':'%s','method':'%s'}]", firstGetUrl, "get", secondGetUrl,
                        "get")), "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            String result = EntityUtils.toString(response.getEntity());
            logger.error(" Result bulk metadata get: " + result);
            assertEquals(200, response.getStatusLine().getStatusCode());
            JSONArray jsonArray = new JSONArray(result);
            logger.debug(" json object 0: " + jsonArray.get(0));
            logger.debug(" json object 1: " + jsonArray.get(1));
            JSONObject jsonObject0 = (JSONObject) jsonArray.get(0);
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(1);
            logger.debug(" statusCode object 0: " + jsonObjectGetStringFromInt(jsonObject0, "statusCode"));
            logger.debug(" statusCode object 1: " + jsonObjectGetStringFromInt(jsonObject1, "statusCode"));
            assertEquals("200", jsonObjectGetStringFromInt(jsonObject0, "statusCode"));
            assertEquals("200", jsonObjectGetStringFromInt(jsonObject1, "statusCode"));
        }
    }

    @Test
    public void testUrlEncode() throws IOException {
        // A QName in the path, sent with its braces, slashes and colon encoded to different degrees. The web script
        // runtime decodes the path once, as for a direct request.
        String name = "{http://www.alfresco.org/model/content/1.0}name";
        JsonNode results = bulk(makeBulkUrl(), json(String.format(
                "[{'url':'%s','method':'get'},{'url':'%s','method':'get'},{'url':'%s','method':'get'}]",
                "/dictionary/properties/%7Bhttp://www.alfresco.org/model/content/1.0%7Dname",
                "/dictionary/properties/" + ContentModel.PROP_OWNER,
                "/dictionary/properties/%7Bhttp%3A%2F%2Fwww.alfresco.org%2Fmodel%2Fcontent%2F1.0%7Dname")));

        assertEquals(3, results.size());
        assertEquals(name, assertSubResult(results.get(0), 200).get("body").get("name").asText());
        assertEquals(ContentModel.PROP_OWNER.toString(),
                assertSubResult(results.get(1), 200).get("body").get("name").asText());
        assertEquals(name, assertSubResult(results.get(2), 200).get("body").get("name").asText());
    }

    private String removePrefixAndAuthenticate(String fullUrl) {
        String prefix = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1";
        String ret = fullUrl.replace(prefix, "");
        return ret;
    }

    @Test
    public void testPostBulk() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME));
        final ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());
        final NodeRef parentRef = primaryParentAssoc.getTarget();

        final String url = makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        String secondPostUrl = removePrefixAndAuthenticate(makeNodesUrl(parentRef, "/metadata", "admin", "admin"));
        String firstJsonBody = String.format("{'aspectsToAdd':['%s']}", ContentModel.ASPECT_VERSIONABLE);
        String secondJsonBody = String.format("{'aspectsToAdd':['%s']}", ContentModel.ASPECT_VERSIONABLE);
        String jsonString = json(
                String.format("[{'url':'%s','method':'%s','body':%s},{'url':'%s','method':'%s','body':%s}]",
                        firstPostUrl, "post", firstJsonBody,
                        secondPostUrl, "post", secondJsonBody));
        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.debug(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertTrue(alfrescoNodeService
                                    .hasAspect(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue()),
                                            ContentModel.ASPECT_VERSIONABLE));
                            assertTrue(alfrescoNodeService
                                    .hasAspect(new org.alfresco.service.cmr.repository.NodeRef(parentRef.getValue()),
                                            ContentModel.ASPECT_VERSIONABLE));
                        }
                        return null;
                    }
                }, false, true);

        firstPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));
        secondPostUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));

        firstJsonBody = String.format("{'propertiesToSet':{'%s':['newName']}}", ContentModel.PROP_NAME);
        jsonString = json(String.format("[{'url':'%s','method':'%s','body':%s}]",
                firstPostUrl, "post", firstJsonBody));
        final HttpPost httpPost2 = new HttpPost(url);
        httpPost2.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost2)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.debug(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertEquals("newName", alfrescoNodeService
                                    .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME).getValue()),
                                            ContentModel.PROP_NAME));
                        }
                        return null;
                    }
                }, false, true);

        secondJsonBody = String.format("{'propertiesToSet':{'%s':['newName']}}", ContentModel.PROP_NAME);
        jsonString = json(String.format("[{'url':'%s','method':'%s','body':%s}]",
                secondPostUrl, "post", secondJsonBody));
        final HttpPost httpPost3 = new HttpPost(url);
        httpPost3.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost3)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.debug(" Result bulk metadata post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertNotEquals("newName", alfrescoNodeService
                                    .getProperty(new org.alfresco.service.cmr.repository.NodeRef(
                                                    initializedNodeRefs.get(RestV1BaseTest.TESTFILE3_NAME).getValue()),
                                            ContentModel.PROP_NAME));
                        }
                        return null;
                    }
                }, false, true);

    }

    @Test
    public void testSubError() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();
        final String firstUrlForRename = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/metadata", "admin", "admin"));
        final String secondUrlForError = removePrefixAndAuthenticate(
                makeNodesUrl("c03be21a-ebfc-4486-a98a-0d3cae2c40ca", "/metadata", "admin", "admin"));
        final String thirdUrlForRename = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME), "/metadata", "admin", "admin"));
        final String firstPostBody = String
                .format("{'propertiesToSet':{'%s':['testName1']}}", ContentModel.PROP_NAME);
        final String secndPostBody = String
                .format("{'propertiesToSet':{'%s':['testName2']}}", ContentModel.PROP_NAME);
        final String thirdPostBody = String
                .format("{'propertiesToSet':{'%s':['testName3']}}", ContentModel.PROP_NAME);
        final String comboPostBody = String.format("[{'url':'%s', 'method': 'post', 'body':%s}," +
                        "{'url':'%s', 'method': 'get', 'body':%s}," +
                        "{'url':'%s', 'method': 'post', 'body':%s}]",
                firstUrlForRename, firstPostBody,
                secondUrlForError, secndPostBody,
                thirdUrlForRename, thirdPostBody);

        final String url = makeBulkUrl();
        final HttpPost post = new HttpPost(url);

        StringEntity entity = new StringEntity(json(comboPostBody), "UTF-8");
        entity.setContentType("application/json");
        post.setEntity(entity);
        CloseableHttpClient client = HttpClients.createDefault();
        logger.info("post getEntity {}", post.getEntity());
        CloseableHttpResponse response = client.execute(post);

        assertEquals(200, response.getStatusLine().getStatusCode());

        JsonNode node = new ObjectMapper().readTree(response.getEntity().getContent());
        assertEquals(404, node.get(1).get("statusCode").asInt());
    }

    @Test
    public void testPutBulk() throws IOException {
        HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME));
        ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        NodeRef folderRef = primaryParentAssoc.getTarget();

        parentAssociations = this.nodeService.getParentAssociations(folderRef);
        primaryParentAssoc = parentAssociations.get(0);
        final NodeRef mainFolderRef = primaryParentAssoc.getTarget();

        List<ChildParentAssociation> childAssocs = this.nodeService.getChildAssociations(mainFolderRef);
        assertEquals(3, childAssocs.size());

        String url = this.makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstPutUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/parent", "admin", "admin"));
        String firstJsonBody = String.format("{'parent':'%s'}", mainFolderRef);
        String jsonString = json(
                String.format("[{'url':'%s', 'method':'%s', 'body':%s}]", firstPutUrl, "put", firstJsonBody));
        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            List<ChildParentAssociation> newChildAssocs = nodeService
                                    .getChildAssociations(mainFolderRef);
                            assertEquals(4, newChildAssocs.size());
                        }
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testDelete() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        List<ChildParentAssociation> parentAssociations = this.nodeService
                .getParentAssociations(initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME));
        ChildParentAssociation primaryParentAssoc = parentAssociations.get(0);
        final NodeRef folderRef = primaryParentAssoc.getTarget();

        List<ChildParentAssociation> childAssocs = this.nodeService.getChildAssociations(folderRef);
        assertEquals(2, childAssocs.size());

        String url = this.makeBulkUrl();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        String firstDeleteUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE2_NAME), "admin", "admin"));
        String jsonString = json(String.format("[{'url':'%s', 'method':'%s'}]", firstDeleteUrl, "delete"));
        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(folderRef);
                            assertEquals(1, newChildAssocs.size());
                        }
                        return null;
                    }
                }, false, true);
        String secondDeleteUrl = removePrefixAndAuthenticate(
                makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "?permanently=true", "admin",
                        "admin"));
        jsonString = json(String.format("[{'url':'%s', 'method':'%s', 'body':{}}]", secondDeleteUrl, "delete"));
        final HttpPost httpPost2 = new HttpPost(url);
        httpPost2.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        try (CloseableHttpResponse response = httpclient.execute(httpPost2)) {
                            String result = EntityUtils.toString(response.getEntity());
                            logger.debug(" Result bulk delete post: " + result);
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            assertFalse(alfrescoNodeService
                                    .exists(new org.alfresco.service.cmr.repository.NodeRef(
                                            initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME).getValue())));
                        }
                        return null;
                    }
                });
    }

    @Test
    public void testNativeEndpointsReturnTheSameAsDirectRequests() throws IOException {
        String[] urls = {"/version", "/people?userName=admin", "/dictionary/namespaces"};
        JsonNode results = bulk(makeBulkUrl(), json(String.format(
                "[{'url':'%s','method':'get'},{'url':'%s','method':'get'},{'url':'%s','method':'get'}]",
                (Object[]) urls)));

        assertEquals(urls.length, results.size());
        for (int i = 0; i < urls.length; i++) {
            JsonNode direct = RestCall.assertJson(RestCall.get(makeAlfrescoBaseurlAdmin() + "/apix/v1" + urls[i]));
            JsonNode result = assertSubResult(results.get(i), 200);
            assertEquals(urls[i], direct, result.get("body"));
            // The direct response's headers; bulk adds the charset it decodes the body in, as before.
            assertEquals("application/json;charset=UTF-8", result.get("headers").get("Content-Type").asText());
            assertEquals("no-cache", result.get("headers").get("Cache-Control").asText());
        }
    }

    @Test
    public void testQueryParametersAreTakenFromTheSubRequestUrl() throws IOException {
        // Parameters of the bulk URL itself are not passed on to the sub-requests.
        JsonNode results = bulk(makeBulkUrl() + "?userName=admin",
                json("[{'url':'/people','method':'get'},{'url':'/people?userName=-me-','method':'get'}]"));

        assertSubResult(results.get(0), 400);
        assertEquals("admin", assertSubResult(results.get(1), 200).get("body").get("userName").asText());
    }

    @Test
    public void testSubRequestsRunAsTheBulkCaller() throws IOException {
        init(); // creates the non-admin user
        String meUrl = json("[{'url':'/people?userName=-me-','method':'get'}]");
        String adminTicket = authenticationService.getCurrentTicket();

        // A ticket on a sub-request URL is ignored: the sub-request runs as the bulk caller, not as the ticket's user.
        JsonNode asUser = bulk(makeAlfrescoBaseurl(USERWITHOUTRIGHTS, USERWITHOUTRIGHTS) + "/apix/v1/bulk",
                json(String.format("[{'url':'/people?userName=-me-','method':'get'},"
                        + "{'url':'/people?userName=-me-&alf_ticket=%s','method':'get'}]", adminTicket)));
        assertEquals(USERWITHOUTRIGHTS,
                assertSubResult(asUser.get(0), 200).get("body").get("userName").asText());
        assertEquals(USERWITHOUTRIGHTS,
                assertSubResult(asUser.get(1), 200).get("body").get("userName").asText());

        // Authenticated by a ticket on the bulk URL: the sub-request URL needs no credentials of its own.
        JsonNode withTicket = bulk(makeBulkUrlWithTicket(), meUrl);
        assertEquals("admin", assertSubResult(withTicket.get(0), 200).get("body").get("userName").asText());
    }

    @Test
    public void testErrorsAreReportedPerSubRequest() throws IOException {
        String unknownUser = "Україна";
        JsonNode results = bulk(makeBulkUrl(), json(String.format("[{'url':'/people?userName=%s','method':'get'},"
                        + "{'url':'/people','method':'get'},"
                        + "{'url':'/does/not/exist','method':'get'},"
                        + "{'url':'/category/aspect/cm:titled','method':'get'},"
                        + "{'url':'/version','method':'get'}]",
                URLEncoder.encode(unknownUser, StandardCharsets.UTF_8))));

        // A plain-text error: the body is reported as a string, decoded as UTF-8, with the response's headers.
        JsonNode notFound = assertSubResult(results.get(0), 404);
        assertEquals("User " + unknownUser + " does not exist", notFound.get("body").asText());
        assertEquals("text/plain;charset=UTF-8", notFound.get("headers").get("Content-Type").asText());
        assertEquals("no-cache", notFound.get("headers").get("Cache-Control").asText());

        // An error status from a web script is rendered as for a direct request: a JSON status document.
        assertEquals(400, assertSubResult(results.get(1), 400).get("body").get("status").get("code").asInt());

        assertSubResult(results.get(2), 404);

        // An unexpected exception: a 500 with the exception message and no headers, as before.
        JsonNode failed = assertSubResult(results.get(3), 500);
        assertTrue(failed.toString(), failed.get("body").asText().startsWith("Exception found: "));
        assertEquals(0, failed.get("headers").size());

        // Sub-requests after a failed one still run.
        assertSubResult(results.get(4), 200);
    }

    @Test
    public void testAFailedSubRequestIsRolledBackWithoutAffectingTheOthers() throws IOException {
        final HashMap<String, NodeRef> nodes = init();
        NodeRef first = nodes.get(RestV1BaseTest.TESTFILE_NAME);
        NodeRef failing = nodes.get(RestV1BaseTest.TESTFILE2_NAME);
        NodeRef last = nodes.get(RestV1BaseTest.TESTFILE3_NAME);
        // Adds cm:titled, then fails: the system aspect sys:pendingDelete cannot be removed.
        String failingBody = String.format("{'aspectsToAdd':['%s'],'aspectsToRemove':['%s']}",
                ContentModel.ASPECT_TITLED, ContentModel.ASPECT_PENDING_DELETE);

        JsonNode results = bulk(makeBulkUrl(), json(String.format(
                "[{'url':'%s','method':'post','body':%s},{'url':'%s','method':'post','body':%s},"
                        + "{'url':'%s','method':'post','body':%s}]",
                removePrefixAndAuthenticate(makeNodesUrl(first, "/metadata", "admin", "admin")),
                renameBody("bulkFirst"),
                removePrefixAndAuthenticate(makeNodesUrl(failing, "/metadata", "admin", "admin")), failingBody,
                removePrefixAndAuthenticate(makeNodesUrl(last, "/metadata", "admin", "admin")),
                renameBody("bulkLast"))));

        assertSubResult(results.get(0), 200);
        assertTrue(results.get(1).toString(), results.get(1).get("statusCode").asInt() >= 400);
        assertSubResult(results.get(2), 200);
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            // Each sub-request has its own transaction: the failed one is rolled back, the others are committed.
            assertFalse(alfrescoNodeService.hasAspect(alfresco(failing), ContentModel.ASPECT_TITLED));
            assertEquals("bulkFirst", alfrescoNodeService.getProperty(alfresco(first), ContentModel.PROP_NAME));
            assertEquals("bulkLast", alfrescoNodeService.getProperty(alfresco(last), ContentModel.PROP_NAME));
            return null;
        }, true, true);
    }

    private static String renameBody(String name) {
        return String.format("{'propertiesToSet':{'%s':['%s']}}", ContentModel.PROP_NAME, name);
    }

    private static org.alfresco.service.cmr.repository.NodeRef alfresco(NodeRef nodeRef) {
        return new org.alfresco.service.cmr.repository.NodeRef(nodeRef.getValue());
    }

    /** Posts a bulk request, checks the bulk response itself and returns the sub-results. */
    private static JsonNode bulk(String bulkUrl, String body) throws IOException {
        JsonNode results = RestCall.assertJson(RestCall.post(bulkUrl, ContentType.APPLICATION_JSON, body));
        assertTrue(results.toString(), results.isArray());
        return results;
    }

    private static JsonNode assertSubResult(JsonNode result, int status) {
        assertEquals(result.toString(), status, result.get("statusCode").asInt());
        return result;
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
