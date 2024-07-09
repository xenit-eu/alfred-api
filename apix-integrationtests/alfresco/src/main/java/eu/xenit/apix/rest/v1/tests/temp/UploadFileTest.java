package eu.xenit.apix.rest.v1.tests.temp;

import static org.junit.Assert.assertEquals;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class UploadFileTest extends RestV1BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileTest.class);
    private static final String LOCAL_TESTFILE_NAME = "test.txt";
    private NodeRef parentNodeRef;
    private Map<String, NodeRef> initNodeRefArray;


    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        initNodeRefArray = init();
        this.parentNodeRef = initNodeRefArray.get(RestV1BaseTest.TESTFOLDER_NAME);
    }

    @Test
    public void testUploadFile() throws IOException {
        String url = createUrl(null, null);
        logger.error("testUploadFile URL: " + url);
        HttpEntity entity = createHttpEntity(parentNodeRef.toString(), LOCAL_TESTFILE_NAME);
        try (CloseableHttpResponse response = doPost(url, entity)) {
            String resultString = EntityUtils.toString(response.getEntity());
            logger.error(" resultString: " + resultString);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testUploadSimpleFile() throws IOException {
        String url = createUrl(null, null);
        url = "http://admin:admin@localhost:8080/alfresco/s/apix/v1/nodes/simpleuploadtest";
        logger.error("testUploadSimpleFile URL: " + url);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("parent", parentNodeRef.toString())
                .addTextBody("guid", parentNodeRef.toString())
                .addTextBody("type", ContentModel.TYPE_CONTENT.toString())
                .addBinaryBody("file", createTestFile(LOCAL_TESTFILE_NAME)) //, ContentType.APPLICATION_OCTET_STREAM, "file.txt"
                .build();

        try (CloseableHttpResponse response = doPost(url, entity)) {
            String resultString = EntityUtils.toString(response.getEntity());
            logger.error(" resultString: " + resultString);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }



    @Test
    public void testUploadFileWhereFileAlreadyExists() throws IOException {
        String url = createUrl("admin", "admin");
        logger.error("testUploadFileWhereFileAlreadyExists >>>>> URL: {}", url);

        HttpEntity entity = createHttpEntity(parentNodeRef.toString(), RestV1BaseTest.TESTFILE_NAME);
        logger.error("testUploadFileWhereFileAlreadyExists >>>>> entity.getContentEncoding: {}", entity.getContentEncoding());
        logger.error("testUploadFileWhereFileAlreadyExists >>>>> entity.getContentType: {}", entity.getContentType());

        try (CloseableHttpResponse response = doPost(url, entity)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testUploadFileResultsInAccessDenied() throws IOException {
        String url = createUrl(RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
        logger.error(">>>>> URL: {}", url);
        HttpEntity entity = createHttpEntity(initNodeRefArray.get(RestV1BaseTest.NOUSERRIGHTS_FILE_NAME).toString(), LOCAL_TESTFILE_NAME);
        try (CloseableHttpResponse response = doPost(url, entity)) {
            String resultString = EntityUtils.toString(response.getEntity());
            logger.error(" resultString: {}", resultString);
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

    @Test
    /* Upload a file and simultaneously set its metadata. */
    public void testUploadFileWithMetadata() throws IOException, JSONException {
        String propertyName = "{http://www.alfresco.org/model/content/1.0}title";
        String propertyValue = "Saifutsuhengoshiatsuken";
        String metadata = String.format("{ 'propertiesToSet': { '%s': ['%s'] }}", propertyName, propertyValue);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("parent", this.parentNodeRef.toString(), ContentType.TEXT_PLAIN)
                .addTextBody("type", ContentModel.TYPE_CONTENT.toString(), ContentType.TEXT_PLAIN)
                .addTextBody("guid", "test string", ContentType.TEXT_PLAIN)
                .addPart("file", new FileBody(createTestFile(LOCAL_TESTFILE_NAME)))
                .addTextBody("metadata", json(metadata), ContentType.APPLICATION_JSON)
                .build();
        logger.error("HttpEntity entity getContent {}", entity.getContent());
        logger.error("HttpEntity entity getContentEncoding {}", entity.getContentEncoding());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(makeAlfrescoBaseurlAdmin() + "/apix/v1/nodes/upload");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            logger.error("response.getEntity = {}", response.getEntity());
            logger.error("response.getStatusLine = {}", response.getStatusLine().getStatusCode());
            EntityUtils.toString(response.getEntity());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }
//setContentType(MediaType.APPLICATION_JSON_UTF8)
    private HttpEntity createHttpEntity(String parentRef, String filename) throws IOException {
        return MultipartEntityBuilder.create()
                .addTextBody("parent", parentRef)
                .addTextBody("type", ContentModel.TYPE_CONTENT.toString())
                .addBinaryBody("file", createTestFile(filename)) //, ContentType.APPLICATION_OCTET_STREAM, "file.txt"
                .build();
    }



    private CloseableHttpResponse doPost(String url, HttpEntity entity) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        logger.error("httpPost.setEntity(entity): {}", entity.getContentType());
        return httpClient.execute(httpPost);
    }

    private File createTestFile(String pathName) throws IOException {
        File result = new File(pathName);

        Boolean newFileCreated;
        newFileCreated = result.createNewFile();
        if (newFileCreated) {
            logger.error(" Created new file. ");
        } else {
            logger.error(" Did not create new file. ");
        }
        PrintWriter writer = new PrintWriter(pathName, "UTF-8");
        String contentString = "This is the content";
        writer.println(contentString);
        writer.close();
        logger.error("result = {}" , result.isFile());
        return result;
    }
}
