package eu.xenit.apix.rest.v2.tests;

import eu.xenit.testing.integrationtesting.runner.AlfrescoTestRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(AlfrescoTestRunner.class)
public abstract class BaseTest extends eu.xenit.apix.rest.v1.tests.BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private final static String VERSION = "v2";

    @Override
    protected String getVersion() {
        return VERSION;
    }
}
