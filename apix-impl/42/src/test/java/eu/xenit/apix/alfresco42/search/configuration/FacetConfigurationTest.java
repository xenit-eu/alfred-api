package eu.xenit.apix.alfresco42.search.configuration;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Stan on 19-Feb-16.
 */
public class FacetConfigurationTest {

    @Test
    public void getFacetConfigurationTest() {
        FacetConfiguration facetConfiguration = new FacetConfiguration();

        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("facet-forms-config.json");

        if (input == null) {
            Assert.fail("Unable to read in facet-forms-config.json");
        }

        List<String> config = facetConfiguration.getFacetConfig(input);

        if (config.isEmpty()) {
            Assert.fail("Empty result");
        }

        if (config.size() != 7) {
            Assert.fail("return value is wrong size");
        }

        Assert.assertTrue(true);
    }
}
