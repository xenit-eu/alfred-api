package eu.xenit.apix.webscripts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Michiel Huygen on 31/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class AnnotationWebscriptBuilderTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    AnnotationWebScriptBuilder builder;

    @Autowired
    TestDEWebscript1 webscript;

    @Test
    public void TestCreateAnnotatedWebscript() throws NoSuchMethodException, IOException {

//        String[] names = applicationContext.getBeanNamesForType(webscript.getClass());
//        if (names.length == 0)
//            throw new RuntimeException("Can't find bean name for bean type: " + webscript.getClass());
//        if (names.length > 1)
//            throw new RuntimeException("Found multiple beans for bean type: " + webscript.getClass());

        AnnotationWebScript webscript = builder.createWebscriptForMethod("testGet", this.webscript);

        System.out.println(webscript);
        webscript.execute(mock(WebScriptRequest.class), mock(WebScriptResponse.class));

        assertEquals(1, TestDEWebscript1.count);


    }
}
