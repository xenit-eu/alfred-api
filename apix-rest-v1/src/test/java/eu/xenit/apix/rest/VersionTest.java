package eu.xenit.apix.rest;

import eu.xenit.apix.version.VersionDescription;
import org.junit.Test;


/**
 * Created by Michiel Huygen on 20/05/2016.
 */
public class VersionTest {

    @Test
    public void TestSplitVersion() {
        VersionDescription desc = VersionDescription.createFromVersionString("1.4.2-78", "desc");
        assertEquals(1, desc.getMajor());
        assertEquals(4, desc.getMinor());
        assertEquals(2, desc.getPatch());

        desc = VersionDescription.createFromVersionString("1.4.2", "desc");
        assertEquals(1, desc.getMajor());
        assertEquals(4, desc.getMinor());
        assertEquals(2, desc.getPatch());

        desc = VersionDescription.createFromVersionString("1.4.2-hello", "desc");
        assertEquals(1, desc.getMajor());
        assertEquals(4, desc.getMinor());
        assertEquals(2, desc.getPatch());

    }
}
