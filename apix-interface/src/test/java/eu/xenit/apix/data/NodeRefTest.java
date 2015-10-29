package eu.xenit.apix.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michiel Huygen on 16/06/2016.
 */
public class NodeRefTest {

    @Test
    public void TestParse() {
        NodeRef n = new NodeRef("workspace://SpacesStore/a38308f8-6f30-4d8a-8576-eaf6703fb9d3");
        assertEquals("workspace", n.getStoreRefProtocol());
        assertEquals("SpacesStore", n.getStoreRefId());
        assertEquals("a38308f8-6f30-4d8a-8576-eaf6703fb9d3", n.getGuid());

        n = new NodeRef("archive://VersionStore/a38308f8-6f30-4d8a-8576-eaf6703fb9d3");
        assertEquals("archive", n.getStoreRefProtocol());
        assertEquals("VersionStore", n.getStoreRefId());
        assertEquals("a38308f8-6f30-4d8a-8576-eaf6703fb9d3", n.getGuid());
    }

    @Test
    public void TestParseMultiTenant() {
        NodeRef n = new NodeRef("workspace://SpacesStore@tenant/a38308f8-6f30-4d8a-8576-eaf6703fb9d3");
        assertEquals("workspace", n.getStoreRefProtocol());
        assertEquals("SpacesStore@tenant", n.getStoreRefId());
        assertEquals("a38308f8-6f30-4d8a-8576-eaf6703fb9d3", n.getGuid());
    }

}