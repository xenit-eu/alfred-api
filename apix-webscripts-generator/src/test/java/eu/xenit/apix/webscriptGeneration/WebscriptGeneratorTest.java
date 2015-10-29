package eu.xenit.apix.webscriptGeneration;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
public class WebscriptGeneratorTest {

    private WebscriptGenerator gen;

    public static String cleanupLines(
            String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("(?m)^[\\s&&[^\\n]]+|[\\s+&&[^\\n]]+$", "").replace("\r\n", "\n");
    }

    @Before
    public void setUp() throws Exception {
        gen = new WebscriptGenerator();
    }

    @Test
    public void TestGenerateDescXmlFilename() {
        WebscriptDefinition def = new WebscriptDefinition();

        def.setId("testScript");
        def.setPackage("eu.xenit.apix.webscriptGeneration");
        def.setMethod("get");
        assertEquals("eu/xenit/apix/webscriptGeneration/testScript.get.desc.xml", gen.generateDescXmlFilename(def));

        def.setId("testScript2");
        def.setPackage("eu.xenit.apix.webscriptGeneration");
        assertEquals("eu/xenit/apix/webscriptGeneration/testScript2.get.desc.xml", gen.generateDescXmlFilename(def));

        def.setMethod("post");
        def.setPackage("eu.xenit.apix.webgen");
        assertEquals("eu/xenit/apix/webgen/testScript2.post.desc.xml", gen.generateDescXmlFilename(def));

        def.setMethod("delete");
        def.setPackage("eu.xenit.apix.webscriptGeneration");
        assertEquals("eu/xenit/apix/webscriptGeneration/testScript2.delete.desc.xml", gen.generateDescXmlFilename(def));

        def.setMethod("put");
        def.setPackage("eu.xenit.apix.webscriptGeneration");
        assertEquals("eu/xenit/apix/webscriptGeneration/testScript2.put.desc.xml", gen.generateDescXmlFilename(def));
    }

    @Test
    public void TestGenerateDescXml() {
        WebscriptDefinition def = new WebscriptDefinition();

        def.setId("testScript");
        def.setMethod("get");
        def.setShortName("Webscript in unit test");
        def.setDescription("Webscript in unit test description");
        def.setUrl("/home/is/a/{parameterized}/url");
        def.setAuthentication("none");
        def.setFamily("We are fam-i-ly!");

        String expectedContent = "<webscript>\n" +
                "    <shortname>Webscript in unit test</shortname>\n" +
                "    <description>Webscript in unit test description</description>\n" +
                "    <url>/home/is/a/{parameterized}/url</url>\n" +
                "    <authentication>none</authentication>\n" +
                "    <format default=\"\">argument</format>\n" +
                "    <family>We are fam-i-ly!</family>\n" +
                "</webscript>";

        String content = gen.generateDescXmlContent(def);

        assertEquals(cleanupLines(expectedContent),
                cleanupLines(content));

    }

    @Test
    public void TestGenerateSpringXml() {
        WebscriptDefinition def1 = new WebscriptDefinition();
        WebscriptDefinition def2 = new WebscriptDefinition();

        def1.setId("simpleTest1");
        def1.setPackage("eu.xenit.apix.webgen.test");
        def1.setMethod("get");

        def2.setId("simpleTest2");
        def2.setPackage("eu.xenit.apix.webgen.test");
        def2.setMethod("post");

        /*String expectedContents = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'>\n" +
                "<beans>\n" +
                "    <bean id=\"webscript.eu.xenit.apix.webgen.test.simpleTest1.get\"\n" +
                "          class=\"eu.xenit.apix.webgen.test.simpleTest1GET\"\n" +
                "          parent=\"webscript\">\n" +
                "    </bean>\n" +
                "    <bean id=\"webscript.eu.xenit.apix.webgen.test.simpleTest2.post\"\n" +
                "          class=\"eu.xenit.apix.webgen.test.simpleTest2POST\"\n" +
                "          parent=\"webscript\">\n" +
                "    </bean>\n" +
                "</beans>\n";*/

        String contents = gen.generateSpringXml(Arrays.asList(def1, def2));

        /*assertEquals(cleanupLines(expectedContents),
                cleanupLines(contents));*/
        assertTrue(contents.contains("bean id=\"webscript.eu.xenit.apix.webgen.test.simpleTest1.get"));
        assertTrue(contents.contains("bean id=\"webscript.eu.xenit.apix.webgen.test.simpleTest2.post"));
    }

    @Test(expected = Exception.class)
    public void TestGenerateSpringXml_DontAllowEmptyPackage1() {
        WebscriptDefinition def1 = new WebscriptDefinition();
        def1.setId("simpleTest1");
        def1.setPackage("");
        def1.setMethod("get");

        String contents = gen.generateSpringXml(Arrays.asList(def1));
    }

    @Test(expected = Exception.class)
    public void TestGenerateSpringXml_DontAllowEmptyPackage2() {
        WebscriptDefinition def1 = new WebscriptDefinition();
        def1.setId("simpleTest1");
        def1.setPackage(null);
        def1.setMethod("get");

        String contents = gen.generateSpringXml(Arrays.asList(def1));
    }

    @Test
    public void TestGenerateJavaAdapterClass_Filename() {
        WebscriptDefinition def = new WebscriptDefinition();

        def.setId("testScript");
        def.setPackage("eu.xenit.apix.webscriptGeneration");
        def.setMethod("get");
        assertEquals("eu/xenit/apix/webscriptGeneration/testScriptGET.java", gen.generateJavaClassFilename(def));

        def.setMethod("post");
        def.setPackage("eu.xenit.apix.webgen");
        assertEquals("eu/xenit/apix/webgen/testScriptPOST.java", gen.generateJavaClassFilename(def));

    }

    @Test
    public void TestGenerateJavaAdapterClass() throws NoSuchMethodException {
        WebscriptDefinition def = new WebscriptDefinition();

        def.setId("testScript");
        def.setMethod("post");
        def.setPackage("eu.xenit.apix.webgen.test");
        def.setClazz(TestDEWebscript1.class);
        def.setTargetMethod(TestDEWebscript1.class.getMethod("testGet"));

        //String expectedContents = "";

        String contents = gen.generateJavaClass(def);
        System.out.println(contents);

        assertNotNull(contents);
        assertNotEquals("", contents);
        assertFalse("There are still mustache templates in the java output", contents.contains("{{"));
        assertTrue("Does not contain correct class definition", contents.contains("class testScriptPOST "));
        assertTrue("Does not have correct package", contents.contains("package eu.xenit.apix.webgen.test"));

        // No checking for this at this point, since this is complex + compiler checks this too

//        assertEquals(cleanupLines(expectedContents),
//                cleanupLines(contents));

    }
}

