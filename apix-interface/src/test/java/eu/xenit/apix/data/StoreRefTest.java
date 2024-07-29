package eu.xenit.apix.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Created by Giovanni on 06/09/16.
 */
public class StoreRefTest {

    @Test
    public void TestParse() {
        StoreRef n = new StoreRef("workspace://SpacesStore");
        assertEquals("workspace", n.getProtocol());
        assertEquals("SpacesStore", n.getId());

        n = new StoreRef("archive://VersionStore");
        assertEquals("archive", n.getProtocol());
        assertEquals("VersionStore", n.getId());
    }

    @Test
    public void TestParseMultiTenant() {
        StoreRef n = new StoreRef("workspace://SpacesStore@tenant");
        assertEquals("workspace", n.getProtocol());
        assertEquals("SpacesStore@tenant", n.getId());
    }
}
