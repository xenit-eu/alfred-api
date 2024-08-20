package eu.xenit.alfred.api.rest;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;

@EnableAlfrescoMvcRest(
        @AlfrescoDispatcherWebscript(
                name = "alfred.api",
                servletContext = AlfredApiRestServletContext.class
        )
)
public class SpringConfig {

}