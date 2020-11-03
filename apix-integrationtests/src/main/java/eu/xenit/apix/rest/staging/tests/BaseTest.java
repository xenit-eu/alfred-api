package eu.xenit.apix.rest.staging.tests;

import eu.xenit.apix.integrationtesting.runner.ApixIntegration;
//import eu.xenit.apix.integrationtesting.runner.UseSpringContextOfBundle;
import eu.xenit.apix.tests.ApixImplBundleFilter;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ApixIntegration.class)
//@UseSpringContextOfBundle(filter = ApixImplBundleFilter.class)
public abstract class BaseTest extends eu.xenit.apix.rest.v1.tests.BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private final static String VERSION = "staging";

    @Override
    protected String getVersion() {
        return VERSION;
    }
}
