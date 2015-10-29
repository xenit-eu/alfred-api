package eu.xenit.apix.webscriptGeneration;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
public class WebscriptDefinitionExtractorTest {

    private WebscriptDefinitionExtractor extractor;
    private WebscriptDefinition def1;
    private WebscriptDefinition def2;
    private WebscriptDefinition def3;

    @Before
    public void setUp() throws Exception {
        extractor = new WebscriptDefinitionExtractor();
        java.util.List<WebscriptDefinition> defs = extractor.extractWebscripts(TestDEWebscript1.class);
        assertEquals(2, defs.size());

        def1 = defs.get(0);
        def2 = defs.get(1);

        // Method order is undeterministic in reflection, so swap if needed
        if (!def1.getId().contains("Get")) {
            WebscriptDefinition swap = def2;
            def2 = def1;
            def1 = swap;
        }

        defs = extractor.extractWebscripts(TestDEWebscript2.class);
        def3 = defs.get(0);


    }

    @Test
    public void TestUriAnnotation() {
        assertEquals("/base/method", def1.getUrl());
        assertEquals("get", def1.getMethod());
        assertEquals("My Family", def1.getFamily());

        //assertEquals("",        def1.getDescription());
        assertEquals("TestDEWebscript1_testGet", def1.getId());
        assertEquals(TestDEWebscript1.class.getPackage().getName(), def1.getPackage());
        assertEquals("TestDEWebscript1_testGet", def1.getShortName());
        assertEquals(TestDEWebscript1.class, def1.getClazz());
        assertEquals("testGet", def1.getTargetmethodname());

        assertEquals("/base/method/{param}", def2.getUrl());
        assertEquals("post", def2.getMethod());
        assertEquals(TestDEWebscript1.class, def1.getClazz());
        assertEquals("testPost", def2.getTargetmethodname());
    }

    @Test
    public void TestWebscriptAnnotation() {
        java.util.List<WebscriptDefinition> defs = extractor.extractWebscripts(TestDEWebscript1.class);
        assertEquals(2, defs.size());
        for (WebscriptDefinition def : defs) {
            assertTrue(def.getUrl().startsWith("/base"));
            assertEquals("My Family", def.getFamily());
            assertEquals(TestDEWebscript1.class.getPackage().getName(), def.getPackage());
        }

    }


    @Test
    public void TestAuthenticationAnnotation() {
        // Test on webscript annotation
        assertEquals("guest", def1.getAuthentication());

        // Test override on uri
        assertEquals("admin", def2.getAuthentication());

        // Test default
        assertEquals("user", def3.getAuthentication());
    }

}
