package eu.xenit.apix.alfresco.web;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.web.IWebUtils;
import org.alfresco.repo.admin.SysAdminParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides utility functions to access information about the current web context
 */
@Component("eu.xenit.apix.web.WebUtils")
@OsgiService
public class WebUtils implements IWebUtils {

    @Autowired
    private SysAdminParams sysAdminParams;

    @Override
    public String getHost() {
        return sysAdminParams.getAlfrescoHost();
    }

    @Override
    public int getPort() {
        return sysAdminParams.getAlfrescoPort();
    }

    @Override
    public String getProtocol() {
        return sysAdminParams.getAlfrescoProtocol();
    }
}
