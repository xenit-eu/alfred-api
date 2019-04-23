package eu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;

import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.node.NodeMetadata;
import org.junit.Test;
import org.mockito.Spy;

public class SetMetadataUnitTestWMocking {

    private static final String BASE_TYPE = "{http://www.alfresco.org/model/system/1.0}base";
    private static final String GRAND_PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type3";
    private static final String PARENT_TYPE = "{http://www.alfresco.org/model/content/1.0}type2";
    private static final String INITIAL_TYPE = "{http://www.alfresco.org/model/content/1.0}type1";

    private static final String ASPECT1 = "{http://www.alfresco.org/model/content/1.0}aspect1";
    private static final String ASPECT2 = "{http://www.alfresco.org/model/content/1.0}aspect2";
    private static final String ASPECT3 = "{http://www.alfresco.org/model/content/1.0}aspect3";
    private static final String ASPECT4 = "{http://www.alfresco.org/model/content/1.0}aspect4";

    @Spy
    private NodeServiceSpy nodeServiceSpy;

    @Test
    public void testGeneralixeTypeWithCleanupEnabled() {
        nodeServiceSpy.setMetadata(any(NodeRef.class), any(MetadataChanges.class));
        verify(nodeServiceSpy).cleanupAspects(any(), any(), any(), anyBoolean());

    }

    static abstract class NodeServiceSpy extends NodeService {
        @Override
        public NodeMetadata setMetadata(NodeRef actionedUponNode, MetadataChanges changesToBeApplied) {
            return null;
        }
    }
}
