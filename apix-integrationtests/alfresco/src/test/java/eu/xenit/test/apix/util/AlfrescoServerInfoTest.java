//package eu.xenit.test.apix.util;
//
//import eu.xenit.apix.util.AlfrescoServerInfo;
//import org.alfresco.service.descriptor.Descriptor;
//import org.alfresco.service.descriptor.DescriptorService;
//import org.junit.Test;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//import eu.xenit.apix.util;
//
//// TODO - seems useless. Decided to comment this out.
//// MARKED FOR REMOVAL.
//// Is a unit test for AlfrescoServerInfo
//public class AlfrescoServerInfoTest {
//    @Test
//    public void testVersionLowerThan6x() {
//        DescriptorService descriptorService = mock(DescriptorService.class);
//        Descriptor descriptor = mock(Descriptor.class);
//        doReturn("6.1.1 (3 rc25a8127-b23)").when(descriptor).getVersion();
//        doReturn(descriptor).when(descriptorService).getServerDescriptor();
//        AlfrescoServerInfo asi = new AlfrescoServerInfo(descriptorService, null);
//        String version = asi.getAlfrescoVersion();
//        assertTrue(version.startsWith("6."));
//    }
//}
