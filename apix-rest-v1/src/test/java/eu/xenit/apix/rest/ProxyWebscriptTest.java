package eu.xenit.apix.rest;

import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScript;
import com.github.dynamicextensionsalfresco.webscripts.HandlerMethods;
import com.github.dynamicextensionsalfresco.webscripts.arguments.HandlerMethodArgumentsResolver;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.DescriptionImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Created by Michiel Huygen on 31/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
@Ignore
public class ProxyWebscriptTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void Test() throws NoSuchMethodException {
        Description description = new DescriptionImpl("test", "short", "desc", "url");

        Object handler = applicationContext.getAutowireCapableBeanFactory().createBean(TestDEWebscript1.class);
        HandlerMethods handlerMethods = new HandlerMethods();
        handlerMethods = handlerMethods.createForUriMethod(handler.getClass().getMethod("testGet"));
        HandlerMethodArgumentsResolver argumentsResolver
                = null;//applicationContext.getAutowireCapableBeanFactory().createBean(HandlerMethodArgumentsResolver.class);

        AnnotationWebScript script = new AnnotationWebScript(description, handler, handlerMethods, argumentsResolver);

        int a = 5;

        for (String s : applicationContext.getBeanDefinitionNames()) {
            System.out.println(s);
        }

    }

}
