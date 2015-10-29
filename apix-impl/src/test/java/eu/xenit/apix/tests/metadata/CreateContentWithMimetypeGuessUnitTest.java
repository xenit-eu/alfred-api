package eu.xenit.apix.tests.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.tests.helperClasses.alfresco.services.AlfrescoServiceRegistryStub;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateContentWithMimetypeGuessUnitTest {

    private static final Logger log = LoggerFactory.getLogger(CreateContentWithMimetypeGuessUnitTest.class);

    public INodeService nodeService;

    public AlfrescoServiceRegistryStub serviceRegistryStub;
    public ApixToAlfrescoConversion apixToAlfrescoConversionStubbed;

    public CreateContentWithMimetypeGuessUnitTest() {
        serviceRegistryStub = new AlfrescoServiceRegistryStub();
        apixToAlfrescoConversionStubbed = new ApixToAlfrescoConversion(serviceRegistryStub);
        nodeService = new NodeService(serviceRegistryStub, apixToAlfrescoConversionStubbed);
    }

    @Test
    public void test_createContentWithMimetypeGuess() {
        String fileName = "test";
        String mimeType = "text/plain";
        String contentStr = "TEST CONTENT";
        String encoding = "UTF-8";
        InputStream inputStream = null;
        serviceRegistryStub.mimetypeServiceStub.setHardMimeType(mimeType);
        try {
            inputStream = new ByteArrayInputStream(contentStr.getBytes(encoding));
            ContentData actualContentData = nodeService.createContentWithMimetypeGuess(inputStream, fileName, encoding);
            assertEquals(actualContentData.getMimetype(),mimeType);
            assertEquals(actualContentData.getEncoding(),encoding);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            log.error("An unsupportedEncodingException was caught", unsupportedEncodingException);
            fail();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
