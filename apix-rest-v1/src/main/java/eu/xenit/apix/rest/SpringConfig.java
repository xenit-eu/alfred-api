package eu.xenit.apix.rest;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;

@EnableAlfrescoMvcRest(
        @AlfrescoDispatcherWebscript(
                name = "alfred-rest.api",
                servletContext = AlfredApiRestServletContext.class
        )
)
public class SpringConfig {}