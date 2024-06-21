package eu.xenit.apix.rest.v2.tests;

import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(AlfrescoTestRunner.class)
public abstract class RestV2BaseTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(RestV2BaseTest.class);
    private final static String VERSION = "v2";

    @Override
    protected String getVersion() {
        return VERSION;
    }
}
