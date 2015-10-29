package eu.xenit.apix.tests;

import eu.xenit.testing.integrationtesting.runner.CustomBundleFilter;
import org.osgi.framework.Bundle;


/**
 * Created by Michiel Huygen on 26/04/2016.
 */
public class ApixImplBundleFilter implements CustomBundleFilter {

    @Override
    public Bundle getBundleToUseAsSpringContext(Bundle[] bundles) {
        Bundle ret = null;

        for (Bundle b : bundles) {
            if (!b.getSymbolicName().startsWith("eu.xenit.apix.impl-")) {
                continue;
            }
            if (ret != null) {
                throw new RuntimeException(
                        "Found multiple implementations of apix on the same server! (not supported)");
            }
            ret = b;
        }
        return ret;
    }
}
