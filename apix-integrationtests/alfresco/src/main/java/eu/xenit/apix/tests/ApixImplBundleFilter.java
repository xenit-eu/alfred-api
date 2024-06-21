//package eu.xenit.apix.tests;
//
//TODO - check if the apic-impl:apix-.. lib needs to be imported like this.
// UseSpringContextOfBundle got commented out
////import eu.xenit.testing.integrationtesting.runner.CustomBundleFilter;
//import org.alfresco.rad.test.AlfrescoTestRunner;
//import org.osgi.framework.Bundle;
//
//
///**
// * Created by Michiel Huygen on 26/04/2016.
// */
//// *CustomBundleFilter: Interface that can be implemented by the user to specify which bundle's context to use for loading the test class
//public class ApixImplBundleFilter implements CustomBundleFilter {
//
//    @Override
//    public Bundle getBundleToUseAsSpringContext(Bundle[] bundles) {
//        final String bundleSymbolicNamePrefix = "eu.xenit.apix.apix-impl-";
//        Bundle result = null;
//        for (Bundle b : bundles) {
//            if (!b.getSymbolicName().startsWith(bundleSymbolicNamePrefix)) {
//                continue;
//            }
//            if (result != null) {
//                throw new RuntimeException("Found multiple Apix implementations on the same server! (not supported)");
//            }
//            result = b;
//        }
//        return result;
//    }
//}
