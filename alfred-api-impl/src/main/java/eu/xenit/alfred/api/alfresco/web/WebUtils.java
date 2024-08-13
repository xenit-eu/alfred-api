package eu.xenit.alfred.api.alfresco.web;

import eu.xenit.alfred.api.web.IWebUtils;
import org.alfresco.repo.admin.SysAdminParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides utility functions to access information about the current web context
 */

@Service("eu.xenit.alfred.api.web.WebUtils")
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
