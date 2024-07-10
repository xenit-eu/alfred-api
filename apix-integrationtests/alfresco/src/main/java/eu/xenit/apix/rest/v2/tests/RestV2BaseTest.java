package eu.xenit.apix.rest.v2.tests;

import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestV2BaseTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(RestV2BaseTest.class);
    private final static String VERSION = "v2";

    @Override
    protected String getVersion() {
        return VERSION;
    }
}
