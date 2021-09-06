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

    protected final String SOLR_SUMMARY_CORE_ALFRESCO = "alfresco";
    protected final String SOLR_SUMMARY_FTS = "FTS";
    protected final String SOLR_SUMMARY_FTS_DIRTY = "Node count whose content needs to be updated";
    protected final String SOLR_SUMMARY_FTS_CLEAN = "Node count whose content is in sync";

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
    public boolean areTransactionsSynced() {
        int alfTransaction = getAlfTransactionId();
        int solrTransaction = solrAdminClient.getLastTransactionId();
        logger.debug("alf transaction: {}, solr transaction: {}", alfTransaction, solrTransaction);
        return alfTransaction <= solrTransaction;
    }

    /**
     * Wait until Solr has indexed the latest Alfresco transaction.
     */
    @Override
    public void waitForTransactionSync() throws InterruptedException {
        for (int i = 0; i < maxTries; i++) {
            if (areTransactionsSynced()) {
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
    public int getNumberOfFtsStatusCleanDocs() {
        return (Integer) getSummaryFtsSection().get(SOLR_SUMMARY_FTS_CLEAN);
    }

    @Override
    public boolean isContentIndexed() {
        JSONObject ftsBlock = getSummaryFtsSection();
        logger.debug("solrSummaryFTSBlock: {}", ftsBlock.toString(4));
        return 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY);
    }

    @Override
    public boolean isContentIndexed(int previousCleanCount) {
        JSONObject ftsBlock = getSummaryFtsSection();
        logger.debug("solrSummaryFTSBlock: {}", ftsBlock.toString(4));
        return 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY)
                && previousCleanCount < ((Integer) ftsBlock.get(SOLR_SUMMARY_FTS_CLEAN));
    }

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

    private int getAlfTransactionId() {
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

    protected JSONObject getSummaryFtsSection() {
        return solrAdminClient.getSolrSummaryJson().getJSONObject(SOLR_SUMMARY_CORE_ALFRESCO).getJSONObject(SOLR_SUMMARY_FTS);
    }
}
