package eu.xenit.apix.rest.v1.tests.temp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Before;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class UploadFileTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(UploadFileTest.class);
    private static final String LOCAL_TESTFILE_NAME = "test.txt";

    @Autowired
    ServiceRegistry serviceRegistry;

    @Autowired
    INodeService nodeService;
    private RetryingTransactionHelper transactionHelper;
    private NodeService alfrescoNodeService;
    private ContentService contentService;
    private NodeRef parentNodeRef;
    private Map<String, NodeRef> initNodeRefArray;

    @org.junit.Before
    public void setUp() {
        transactionHelper = this.serviceRegistry.getRetryingTransactionHelper();
        alfrescoNodeService = this.serviceRegistry.getNodeService();
        contentService = this.serviceRegistry.getContentService();

        initNodeRefArray = init();
        this.parentNodeRef = initNodeRefArray.get(RestV1BaseTest.TESTFOLDER_NAME);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testUploadFile() throws IOException {
        String url = createUrl(null, null);
        logger.info(" URL: " + url);
        HttpEntity entity = createHttpEntity(parentNodeRef.toString(), LOCAL_TESTFILE_NAME);
        try (CloseableHttpResponse response = doPost(url, entity)) {
            String resultString = EntityUtils.toString(response.getEntity());
            logger.debug(" resultString: " + resultString);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testUploadFileWhereFileAlreadyExists() throws IOException {
        String url = createUrl("admin", "admin");
        HttpEntity entity = createHttpEntity(parentNodeRef.toString(), RestV1BaseTest.TESTFILE_NAME);
        try (CloseableHttpResponse response = doPost(url, entity)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testUploadFileResultsInAccessDenied() throws IOException {
        String url = createUrl(RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
        logger.info(">>>>> URL: " + url);
        HttpEntity entity = createHttpEntity(initNodeRefArray.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME).toString(), LOCAL_TESTFILE_NAME);
        try (CloseableHttpResponse response = doPost(url, entity)) {
            String resultString = EntityUtils.toString(response.getEntity());
            logger.debug(" resultString: " + resultString);
            assertEquals(403, response.getStatusLine().getStatusCode());
        }
    }

    private String createUrl(String username, String password) {
        StringBuilder urlBuilder = new StringBuilder().append("/apix/v1/nodes/upload");
        if (username == null || password == null) {
            urlBuilder.insert(0, makeAlfrescoBaseurlAdmin());
        } else {
            urlBuilder.insert(0, makeAlfrescoBaseurl(username, password));
        }
        return urlBuilder.toString();
    }

    private HttpEntity createHttpEntity(String parentRef, String filename) throws IOException {
        return MultipartEntityBuilder.create()
                .addTextBody("parent", parentRef)
                .addTextBody("type", ContentModel.TYPE_CONTENT.toString())
                .addBinaryBody("file", createTestFile(filename))
                .build();
    }

    private CloseableHttpResponse doPost(String url, HttpEntity entity) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        return httpClient.execute(httpPost);
    }

    @Test
    /** Upload a file and simulteanously set its metadata. */
    public void testUploadFileWithMetadata() throws IOException, JSONException {
        String property_name = "{http://www.alfresco.org/model/content/1.0}title";
        String property_value = "Saifutsuhengoshiatsuken";
        String metadata = String.format("{ 'propertiesToSet': { '%s': ['%s'] }}", property_name, property_value);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("parent", this.parentNodeRef.toString())
                .addTextBody("type", ContentModel.TYPE_CONTENT.toString())
                .addBinaryBody("file", createTestFile(LOCAL_TESTFILE_NAME))
                .addTextBody("metadata", json(metadata))
                .build();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(makeAlfrescoBaseurlAdmin() + "/apix/v1/nodes/upload");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String resultString = EntityUtils.toString(response.getEntity());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    private File createTestFile(String pathName) throws IOException {
        File result = new File(pathName);

        Boolean newFileCreated;
        newFileCreated = result.createNewFile();
        if (newFileCreated) {
            logger.debug(" Created new file. ");
        } else {
            logger.debug(" Did not create new file. ");
        }
        PrintWriter writer = new PrintWriter(pathName, "UTF-8");
        String contentString = "This is the content";
        writer.println(contentString);
        writer.close();
        return result;
    }

    private File createTempMail() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        logger.debug("ClassLoader classLoader = getClass().getClassLoader()");
        URL mail = classLoader.getResource("cyrillic_message.msg");
        logger.debug("Email: " + mail.getPath());
        InputStream inputStream = mail.openStream();
        logger.debug("Input stream is available:" + inputStream.available());
        logger.debug("InputStream inputStream = classLoader.getResourceAsStream(\"cyrillic_message.msg\")");

        File tempFile = File.createTempFile("test", "msg");
        logger.debug("File tempFile = File.createTempFile(\"test\", \"msg\");");
        logger.debug("tempFile name: " + tempFile.getName());
        tempFile.deleteOnExit();
        logger.debug("tempFile.deleteOnExit()");
        FileOutputStream out = new FileOutputStream(tempFile);
        logger.debug("FileOutputStream out = new FileOutputStream(tempFile);");
        IOUtils.copy(inputStream, out);
        logger.debug("Copied content to tempFile");

        out.flush();
        out.close();
        inputStream.close();

        logger.debug("Flushed and closed input and output streams.");
        return tempFile;
    }
}
