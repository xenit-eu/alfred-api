package eu.xenit.apix.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should only be used by the integration tests.
 */
public abstract class SolrTestHelperBaseImpl implements SolrTestHelper {
    private Logger logger = LoggerFactory.getLogger(SolrTestHelperBaseImpl.class);

    private DataSource dataSource;
    protected SolrAdminClient solrAdminClient;

    private int maxTries = 20;

    public SolrTestHelperBaseImpl(String baseUrl, DataSource dataSource, SwitchableApplicationContextFactory searchSubSystem) {
        this.dataSource = dataSource;
        solrAdminClient = new SolrAdminClient(baseUrl, searchSubSystem);
    }

    /**
     * Beware of calling this method too frequently since it runs an aggregate query over the entire alf_nodes table
     *
     * @return true if the latest Alfresco transaction has been indexed by Solr
     */
    @Override
    public boolean areTxnsSynced() {
        int alfTx = getAlfTxId();
        int solrTx = solrAdminClient.getLastTxId();
        logger.debug("alf tx: {}, solr tx: {}", alfTx, solrTx);
        return alfTx <= solrTx;
    }

    /**
     * Wait until Solr has indexed the latest Alfresco transaction.
     */
    @Override
    public void waitForTxnSync() throws InterruptedException {
        for (int i = 0; i < maxTries; i++) {
            if (areTxnsSynced()) {
                return;
            }
            // These prints are here to send data over the wire while waiting.
            // This prevents any http proxy from closing the connection due to timeouts
            System.out.print("Waiting 5 seconds for Solr to index transactions");
            for (int j = 0; j < 5; j++) {
                System.out.print("..." + ((i * 5) + j + 1));
                Thread.sleep(1000);
            }
            System.out.println();
        }
    }

    @Override
    public abstract int getFtsStatusCleanDocs();

    @Override
    public abstract boolean isContentIndexed();

    @Override
    public abstract boolean isContentIndexed(int previousCleanCount);

    @Override
    public void waitForContentSync() throws InterruptedException {
        for (int i = 0; i < maxTries; i++) {
            if (isContentIndexed()) {
                return;
            }
            System.out.print("Waiting 5 seconds for Solr to index content");
            for (int j = 0; j < 5; j++) {
                System.out.print("..." + ((i * 5) + j + 1));
                Thread.sleep(1000);
            }
            System.out.println();
        }
    }

    @Override
    public void waitForContentSync(int previousCleanCount) throws InterruptedException {
        logger.debug("previousCleanCount: {}", previousCleanCount);
        for (int i = 0; i < maxTries; i++) {
            if (isContentIndexed(previousCleanCount)) {
                return;
            }
            System.out.print("Waiting 5 seconds for Solr to index content");
            for (int j = 0; j < 5; j++) {
                System.out.print("..." + ((i * 5) + j + 1));
                Thread.sleep(1000);
            }
            System.out.println();
        }
    }

    private int getAlfTxId() {
        try (Connection connection = dataSource.getConnection()) {
            final Statement stmt = connection.createStatement();
            final ResultSet rs = stmt.executeQuery("select max( transaction_id ) from alf_node");

            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
