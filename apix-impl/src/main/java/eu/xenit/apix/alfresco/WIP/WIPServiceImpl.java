package eu.xenit.apix.alfresco.WIP;

import eu.xenit.apix.WIP.IWIPService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jasper on 29/09/17.
 */
@Service("eu.xenit.apix.WIP.WIPService")
public class WIPServiceImpl implements IWIPService {

    private final static Logger logger = LoggerFactory.getLogger(WIPServiceImpl.class);
    private ApixToAlfrescoConversion c;
    private WorkflowService wf;
    private ServiceRegistry serviceRegistry;

    @Autowired
    public void setC(ApixToAlfrescoConversion c) {
        this.c = c;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        wf = serviceRegistry.getWorkflowService();
    }


}
