package eu.xenit.apix.util;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@OsgiService
public class AlfrescoServerInfo {
    // Part of Alfresco Private API
    private final DescriptorService descriptorService;
    private final DataSource dataSource;
    private NodeDAO nodeDAO;

    public AlfrescoServerInfo(@Qualifier("descriptorComponent") DescriptorService descriptorServiceParam,
                              DataSource dataSourceParam,
                              NodeDAO nodeDAOParam) {
        descriptorService = descriptorServiceParam;
        dataSource = dataSourceParam;
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
