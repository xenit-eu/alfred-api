package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
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

/**
 * Created by kenneth on 17.03.16.
 */
public class NodeContentTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(NodeContentTest.class);

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
    public void testSetNodeContent() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        HttpPut httpput = new HttpPut(url);
                        HttpEntity httpBody = MultipartEntityBuilder.create()
                                .addBinaryBody("file", "test content123".getBytes(), ContentType.TEXT_PLAIN, "abc.txt")
                                .build();
                        httpput.setEntity(httpBody);

                        try (CloseableHttpResponse response = httpclient.execute(httpput)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                        }

                        return null;
                    }
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        InputStream inputStream = nodeService
                                .getContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)).getInputStream();
                        assertEquals("test content123", IOUtils.toString(inputStream));
                        inputStream.close();
                        return null;
                    }
                }, false, true);

    }

    @Test
    public void testSetNodeContentReturnsAccesDenied() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
                RestV1BaseTest.USERWITHOUTRIGHTS);
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    HttpPut httpput = new HttpPut(url);
                    HttpEntity httpBody = MultipartEntityBuilder.create()
                            .addBinaryBody("file", "test content123".getBytes(), ContentType.TEXT_PLAIN, "abc.txt")
                            .build();
                    httpput.setEntity(httpBody);

                    try (CloseableHttpResponse response = httpclient.execute(httpput)) {
                        assertEquals(403, response.getStatusLine().getStatusCode());
                    }

                    return null;
                }, false, true);
    }

    @Test
    public void testDeleteNodeContent() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        nodeService.setContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                                new ByteArrayInputStream("test contentabc".getBytes()),
                                "abc.txt");
                        return null;
                    }
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        HttpDelete httpDelete = new HttpDelete(url);

                        try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                        }
                        return null;
                    }
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {

                        assertNull(nodeService.getContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME)));
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testDeleteNodeContentReturnsAccesDenied() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
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
    public void testGetNodeContent() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME), "/content", "admin", "admin");
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        nodeService.setContent(initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME),
                                new ByteArrayInputStream("test contentdef".getBytes()),
                                "abc.txt");
                        return null;
                    }
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        HttpGet httpGet = new HttpGet(url);

                        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                            assertEquals(200, response.getStatusLine().getStatusCode());
                            InputStream inputStream = response.getEntity().getContent();
                            assertEquals("test contentdef", IOUtils.toString(inputStream));
                            inputStream.close();
                        }
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void testGetNodeContentReturnsAccesDenied() throws IOException {
        final HashMap<String, NodeRef> initializedNodeRefs = init();

        final String url = makeNodesUrl(initializedNodeRefs.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME), "/content", RestV1BaseTest.USERWITHOUTRIGHTS,
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

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
