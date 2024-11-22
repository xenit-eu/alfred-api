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
import java.nio.charset.Charset;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class NodeContentTest extends RestV1BaseTest {
    @Autowired
    INodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testSetNodeContent() {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(
                initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        int returnedStatusCode = transactionService.getRetryingTransactionHelper()
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
        assertEquals(200, returnedStatusCode);

        final INodeService ns = this.nodeService;
        final NodeRef nodeRef = initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME);
        String content = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    ContentInputStream c = ns.getContent(nodeRef);
                    try(InputStream inputStream = c.getInputStream()) {
                        return IOUtils.toString(inputStream, Charset.defaultCharset());
                    }
                }, false, true);
        assertEquals("This is the content", content);

        String contentHeader = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    ContentInputStream c = ns.getContent(nodeRef);
                    return c.getMimetype();
                }, false, true);
        assertEquals("text/plain", contentHeader);

        // Test the Content-Header of the set file via GET method.
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpGet httpGet = new HttpGet(url);

                    try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        InputStream inputStream = response.getEntity().getContent();
                        assertEquals("This is the content",
                                IOUtils.toString(inputStream, Charset.defaultCharset()));
                        ContentInputStream c = ns.getContent(nodeRef);
                        assertEquals("text/plain",
                                c.getMimetype());
                        inputStream.close();
                    }
                    return null;
                }, false, true);
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
