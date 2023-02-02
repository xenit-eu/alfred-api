package eu.xenit.apix.tests;

import eu.xenit.testing.integrationtesting.runner.CustomBundleFilter;
import org.osgi.framework.Bundle;


/**
 * Created by Michiel Huygen on 26/04/2016.
 */
public class ApixImplBundleFilter implements CustomBundleFilter {

    @Override
    public Bundle getBundleToUseAsSpringContext(Bundle[] bundles) {
        final String bundleSymbolicNamePrefix = "eu.xenit.apix.apix-impl-";
        Bundle result = null;
        for (Bundle b : bundles) {
            if (!b.getSymbolicName().startsWith(bundleSymbolicNamePrefix)) {
                continue;
            }
            if (result != null) {
                throw new RuntimeException("Found multiple Apix implementations on the same server! (not supported)");
            }
            result = b;
        }
        return result;
    }
}
