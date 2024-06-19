package eu.xenit.apix.util;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AlfrescoServerInfoTest {
    @Test
    public void testVersionLowerThan6x() {
        DescriptorService descriptorService = mock(DescriptorService.class);
        Descriptor descriptor = mock(Descriptor.class);
        doReturn("6.1.1 (3 rc25a8127-b23)").when(descriptor).getVersion();
        doReturn(descriptor).when(descriptorService).getServerDescriptor();
        AlfrescoServerInfo asi = new AlfrescoServerInfo(descriptorService, null);
        String version = asi.getAlfrescoVersion();
        assertTrue(version.startsWith("6."));
    }
}
