package eu.xenit.apix.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
// This is a Helperclass for the integrationTests but have to be loaded on the runtimeclasspath!
// This is due to the applicationContext not being able to load via osgi anymore
public class AlfrescoServerInfo {

    // Part of Alfresco Private API
    private final DescriptorService descriptorService;
    @Qualifier("defaultDataSource")
    private final DataSource dataSource;

    public AlfrescoServerInfo(@Qualifier("descriptorComponent") DescriptorService descriptorServiceParam,
            @Qualifier("defaultDataSource") DataSource dataSourceParam) {
        descriptorService = descriptorServiceParam;
        dataSource = dataSourceParam;
    }

    public String getAlfrescoVersion() {
        return descriptorService.getServerDescriptor().getVersion();
    }

    /**
     * Tried using SearchTrackingComponent and NodeDAO to getMaxTxnId, but they are not as consistent and quick as
     * as getting it from alf_node table.
     */
    public long getAlfTransactionIdDAO() {
        try (Connection connection = dataSource.getConnection()) {
            try (final Statement stmt = connection.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery("select max( transaction_id ) from alf_node")) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
