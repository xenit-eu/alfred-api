package eu.xenit.apix.rest.staging.tests;

import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StagingBaseTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(StagingBaseTest.class);
    private final static String VERSION = "staging";

    @Override
    protected String getVersion() {
        return VERSION;
    }
}
