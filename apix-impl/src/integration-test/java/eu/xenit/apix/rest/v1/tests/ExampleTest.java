package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.integrationtesting.runner.ApixIntegration;
import eu.xenit.apix.tests.ApixImplBundleFilter;
import eu.xenit.testing.integrationtesting.runner.UseSpringContextOfBundle;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by kenneth on 14.03.16.
 */
@RunWith(ApixIntegration.class)
@UseSpringContextOfBundle(filter = ApixImplBundleFilter.class)
public class ExampleTest {

    @Test
    public void Test() {

    }
}
