package eu.xenit.alfred.api.rest;

import com.gradecak.alfresco.mvc.rest.config.AlfrescoRestRegistrar;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class AlfredApiWebApplicationContext extends AnnotationConfigWebApplicationContext {
    @Autowired
    @Qualifier("alfred.api")
    private DispatcherWebscript dispatcherWebscript;

    @Override
    protected void initPropertySources() {
        super.initPropertySources();
    }

    @Override
    public ServletContext getServletContext() {
        var servletContext = super.getServletContext();
//        if (servletContext != null) {
////            servletContext.addServlet("alfredApiServlet", DispatcherWebscript.DispatcherWebscriptServlet.class);
//            var servletRegistrations = servletContext.getServletRegistrations();
//        }
        return servletContext;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
//        var servletContext = super.getServletContext();
//        String id = getId();
//        ApplicationContext appContext = getParent();
//        if (appContext != null) {
//            var dispatcherWebscriptBean = appContext.getBean("alfred.api");
//            DispatcherWebscript test1 = appContext.getBean(DispatcherWebscript.class);
//
//            if (servletContext != null) {
//                var servletRegistration = servletContext.addServlet("alfredApiServlet", test1.getDispatcherServlet());
//                servletRegistration.setMultipartConfig(new MultipartConfigElement(""));
//                servletRegistration.addMapping("/s/apix/*", "/service/apix/*", "/wcs/apix/*", "/wcservice/apix/*");
//                servletRegistration.setInitParameter("authenticator", "webscripts.authenticator.remoteuser");
//                var servletRegistrations = servletContext.getServletRegistrations();
//            }
//        }
        super.refresh();
    }

    @Override
    protected void onRefresh() {
        var servletContext = super.getServletContext();
        String id = getId();
        ApplicationContext appContext = getParent();
        if (appContext != null) {
            var dispatcherWebscriptBean = appContext.getBean("alfred.api");
            DispatcherWebscript test1 = appContext.getBean(DispatcherWebscript.class);

            if (servletContext != null) {
                var servletRegistrations = servletContext.getServletRegistrations();
                if (servletRegistrations.containsKey("alfredApiServlet")) {
                    super.onRefresh();
                    return;
                }
                var servletRegistration = servletContext.addServlet("alfredApiServlet", test1.getDispatcherServlet());
                servletRegistration.setMultipartConfig(new MultipartConfigElement(""));
                servletRegistration.addMapping("/s/apix/*", "/service/apix/*", "/wcs/apix/*", "/wcservice/apix/*");
                servletRegistration.setInitParameter("authenticator", "webscripts.authenticator.remoteuser");
            }
        }
        super.onRefresh();
    }

    @Override
    public ServletConfig getServletConfig() {
        var servletConfig = super.getServletConfig();
        return servletConfig;
    }
}
