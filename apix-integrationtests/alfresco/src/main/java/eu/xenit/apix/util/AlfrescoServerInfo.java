package eu.xenit.apix.util;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@OsgiService
public class AlfrescoServerInfo {
    // Part of Alfresco Private API
    private final DescriptorService descriptorService;
    private final NodeDAO nodeDAO;

    public AlfrescoServerInfo(@Qualifier("descriptorComponent") DescriptorService descriptorServiceParam,
                              NodeDAO nodeDAOParam) {
        descriptorService = descriptorServiceParam;
        nodeDAO = nodeDAOParam;
    }

    public String getAlfrescoVersion() {
        return  descriptorService
                .getServerDescriptor()
                .getVersion();
    }

    public long getAlfTransactionIdDAO() {
        return nodeDAO.getMaxTxnId();
    }
}
