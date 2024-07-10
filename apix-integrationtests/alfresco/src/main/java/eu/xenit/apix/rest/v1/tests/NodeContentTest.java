package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import eu.xenit.apix.data.ContentInputStream;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO - fails 2 tests
public class NodeContentTest extends RestV1BaseTest {
    private final static Logger logger = LoggerFactory.getLogger(NodeContentTest.class);
    private INodeService nodeService;

    public NodeContentTest(){
        // initialise the local beans
        nodeService = (INodeService) testApplicationContext.getBean(INodeService.class);
    }
    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testStupidTest() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        // Create the HttpClient
        final HttpClient client = HttpClient.newHttpClient();
        String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/content", "admin", "admin");
        String finalUrl = "http://admin:admin@localhost:8080/alfresco/s/apix/v1/nodes/simpleuploadtest";
        int returnedStatusCode = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    // Set up the basic authentication header
                    String auth = "admin" + ":" + "admin";
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    HttpEntity httpBody = MultipartEntityBuilder.create()
                            .addBinaryBody("file", createTestFile())
                            .build();
                    // Convert HttpEntity to byte array
                    byte[] entityBytes = EntityUtils.toByteArray(httpBody);
                    // Create the HttpRequest with the GET method
                    logger.error("Content-Type {}", httpBody.getContentType().getValue());
                    logger.error("entityBytes {}", HttpRequest.BodyPublishers.ofByteArray(entityBytes));
                    logger.error("url {}", finalUrl);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(finalUrl))
                            .header("Authorization", "Basic " + encodedAuth)
                            .header("Content-Type", httpBody.getContentType().getValue())
                            .POST(HttpRequest.BodyPublishers.ofByteArray(entityBytes))
                            .build();
                    HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return httpResponse.statusCode();
                }, false, true);
        logger.error("returnedStatusCode {}", returnedStatusCode);

        assertEquals(200, returnedStatusCode);
    }



    @Test
    public void testSetNodeContent() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        logger.error("testSetNodeContent url {}", url);
        // Returns code 400...
        int returnedStatusCode = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpPut httpput = new HttpPut(url);
                    HttpEntity httpBody = MultipartEntityBuilder.create()
                            .addBinaryBody(
                                    "file", createTestFile())
                            .build();
                    logger.error("httpBody {}", httpBody);
                    httpput.setEntity(httpBody);
                    try (CloseableHttpResponse response = httpclient.execute(httpput)) {
                        return response.getStatusLine().getStatusCode();
                    }
                }, false, true);
//        // Create the HttpClient
//        final HttpClient client = HttpClient.newHttpClient();
//        int returnedStatusCode = transactionService.getRetryingTransactionHelper()
//            .doInTransaction(() -> {
//                // Set up the basic authentication header
//                String auth = "admin" + ":" + "admin";
//                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//                HttpEntity httpBody = MultipartEntityBuilder.create()
//                    .addBinaryBody("file", createTestFile())
//                    .build();
//                // Convert HttpEntity to byte array
//                byte[] entityBytes = EntityUtils.toByteArray(httpBody);
//                // Create the HttpRequest with the GET method
//                logger.error("Content-Type {}", httpBody.getContentType().getValue());
//                logger.error("entityBytes {}", HttpRequest.BodyPublishers.ofByteArray(entityBytes));
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(url))
//                        .header("Authorization", "Basic " + encodedAuth)
//                        .header("Content-Type", httpBody.getContentType().getValue())
//                        .PUT(HttpRequest.BodyPublishers.ofByteArray(entityBytes))
//                        .build();
//                HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
//                return httpResponse.statusCode();
//            }, false, true);
        logger.error("returnedStatusCode {}", returnedStatusCode);
        assertEquals(200, returnedStatusCode);


        logger.error("reached point 1");

        final INodeService ns = this.nodeService;
        final NodeRef nodeRef = initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME);
        logger.error("nodeRef {} point 1", nodeRef);

        String content = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    ContentInputStream c = ns.getContent(nodeRef);
                    try(InputStream inputStream = c.getInputStream()) {
                        return IOUtils.toString(inputStream, Charset.defaultCharset());
                    }
                }, false, true);
        assertEquals("This is the content", content);
    }

    @Test
    public void testSetNodeContentReturnsAccessDenied() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME),
                "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        int receivedStatusCode = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpPut httpput = new HttpPut(url);
                    HttpEntity httpBody = MultipartEntityBuilder.create()
                            .addBinaryBody(
                                    "file", createTestFile())
                            .build();
                    httpput.setEntity(httpBody);

                    try (CloseableHttpResponse response = httpclient.execute(httpput)) {
                        return response.getStatusLine().getStatusCode();
                    }
                }, false, true);
        assertEquals(403, receivedStatusCode);
    }

    @Test
    public void testDeleteNodeContent() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    nodeService.setContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                            new ByteArrayInputStream("test contentabc".getBytes()),
                            "abc.txt");
                    return null;
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpDelete httpDelete = new HttpDelete(url);

                    try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
                        assertEquals(200, response.getStatusLine().getStatusCode());
                    }
                    return null;
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    assertNull(nodeService.getContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)));
                    return null;
                }, false, true);
    }

    @Test
    public void testDeleteNodeContentReturnsAccesDenied() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME),
                "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    try (CloseableHttpResponse response = httpclient.execute(new HttpDelete(url))) {
                        assertEquals(403, response.getStatusLine().getStatusCode());
                    }
                    return null;
                }, false, true);
    }

    @Test
    public void testGetNodeContent() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final INodeService ns = this.nodeService;
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    ns.setContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                            new ByteArrayInputStream("test contentdef".getBytes()),
                            "abc.txt");
                    return null;
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpGet httpGet = new HttpGet(url);

                    try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        InputStream inputStream = response.getEntity().getContent();
                        assertEquals("test contentdef",
                                IOUtils.toString(inputStream, Charset.defaultCharset()));
                        inputStream.close();
                    }
                    return null;
                }, false, true);
    }

    @Test
    public void testGetNodeContentReturnsAccesDenied() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME),
                "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    try (CloseableHttpResponse response = httpclient.execute(new HttpGet(url))) {
                        assertEquals(403, response.getStatusLine().getStatusCode());
                    }
                    return null;
                }, false, true);
    }

    private File createTestFile() throws IOException {
        String pathName = "test.txt";
        File result = new File(pathName);
        result.createNewFile();
        PrintWriter writer = new PrintWriter(pathName, "UTF-8");
        String contentString = "This is the content";
        writer.print(contentString);
        writer.close();
        return result;
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
