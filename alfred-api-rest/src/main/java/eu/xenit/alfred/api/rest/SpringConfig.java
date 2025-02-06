package eu.xenit.alfred.api.rest;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@EnableAlfrescoMvcRest(
        @AlfrescoDispatcherWebscript(
                name = "alfred.api",
                servletContext = AlfredApiRestServletContext.class,
                servletContextClass = AlfredApiWebApplicationContext.class
        )
)
public class SpringConfig {
}