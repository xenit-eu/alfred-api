package eu.xenit.alfred.api.util;

import java.util.Properties;
import java.util.function.Supplier;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This class should only be used by the integration tests.
 */

@Service
// This is a Helperclass for the integrationTests but have to be loaded on the runtimeclasspath!
// This is due to the applicationContext not being able to load via osgi anymore
public class SolrTestHelperImpl implements SolrTestHelper {

    private static final Logger logger = LoggerFactory.getLogger(SolrTestHelperImpl.class);

    private static final String SOLR_SUMMARY_CORE_ALFRESCO = "alfresco";
    private static final String SOLR_SUMMARY_FTS = "FTS";
    private static final String SOLR_SUMMARY_FTS_DIRTY = "Node count whose content needs to be updated";
    private static final String SOLR_SUMMARY_FTS_CLEAN = "Node count whose content is in sync";
    private static final String SOLR_SUMMARY_FTS_NEW_6 = "Node count with FTSStatus New";
    private static final String SOLR_SUMMARY_FTS_DIRTY_6 = "Node count with FTSStatus Dirty";
    private static final String SOLR_SUMMARY_FTS_CLEAN_6 = "Node count with FTSStatus Clean";

    private final AlfrescoServerInfo alfrescoServerInfo;
    protected SolrAdminClient solrAdminClient;

    private static final int MAX_ATTEMPTS = 20;

    public SolrTestHelperImpl(
            @Qualifier("global-properties") Properties globalProperties,
            @Qualifier("Search") SwitchableApplicationContextFactory searchSubSystem,
            AlfrescoServerInfo alfrescoServerInfoParam) {
        String subsystem = globalProperties.getProperty("index.subsystem.name");
        String solrBaseUrl = subsystem.equals("solr4") ? "/solr4" : "/solr";
        alfrescoServerInfo = alfrescoServerInfoParam;
        solrAdminClient = new SolrAdminClient(solrBaseUrl, searchSubSystem);
    }

    /**
     * Beware of calling this method too frequently since it runs an aggregate query over the entire alf_nodes table
     *
     * @return true if the latest Alfresco transaction has been indexed by Solr
     */
    @Override
    public boolean areTransactionsSynced() {
        long alfTransaction = alfrescoServerInfo.getAlfTransactionIdDAO();
        long solrTransaction = solrAdminClient.getLastTransactionId();
        logger.debug("alf transaction: {}, solr transaction: {}", alfTransaction, solrTransaction);
        return alfTransaction <= solrTransaction;
    }

    /**
     * Wait until Solr has indexed the latest Alfresco transaction.
     */
    @Override
    public void waitForTransactionSync() throws InterruptedException {
        waitForCompletion(this::areTransactionsSynced);
    }

    @Override
    public int getNumberOfFtsStatusCleanDocs() {
        return (Integer) getSummaryFtsSection().get(
                isAlfresco61() ? SOLR_SUMMARY_FTS_CLEAN_6 : SOLR_SUMMARY_FTS_CLEAN
        );
    }

    @Override
    public boolean isContentIndexed() {
        return isContentIndexedImpl(null);
    }

    @Override
    public boolean isContentIndexed(int previousCleanCount) {
        logger.debug("previousCleanCount: {}", previousCleanCount);
        return isContentIndexedImpl(previousCleanCount);
    }

    @Override
    public void waitForContentSync() throws InterruptedException {
        waitForCompletion(() -> isContentIndexedImpl(null));
    }

    @Override
    public void waitForContentSync(int previousCleanCount) throws InterruptedException {
        logger.debug("previousCleanCount: {}", previousCleanCount);
        waitForCompletion(() -> isContentIndexedImpl(previousCleanCount));
    }

    protected JSONObject getSummaryFtsSection() {
        return solrAdminClient
                .getSolrSummaryJson()
                .getJSONObject(SOLR_SUMMARY_CORE_ALFRESCO)
                .getJSONObject(SOLR_SUMMARY_FTS);
    }

    private void waitForCompletion(Supplier<Boolean> hasCompleted) throws InterruptedException {
        System.out.print("Waiting 5 seconds for Solr to index content");
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (hasCompleted.get()) {
                return;
            }
            for (int j = 0; j < 5; j++) {
                System.out.print("..." + ((i * 5) + j + 1));
                Thread.sleep(1000);
            }
            System.out.println();
        }
    }

    private boolean isContentIndexedImpl(Integer previousCleanCount) {
        JSONObject ftsBlock = getSummaryFtsSection();
        logger.debug("solrSummaryFTSBlock: {}", ftsBlock.toString(4));
        if (isAlfresco61()) {
            return 0 == ((Integer) ftsBlock.get(SOLR_SUMMARY_FTS_NEW_6))
                    && 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY_6)
                    && checkPreviousCount(previousCleanCount, (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_CLEAN_6));
        }
        return 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY)
                && checkPreviousCount(previousCleanCount, (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_CLEAN));
    }

    private boolean isAlfresco61() {
        logger.debug("Alfresco version: {}", alfrescoServerInfo.getAlfrescoVersion());
        return alfrescoServerInfo.getAlfrescoVersion().startsWith("6.1");
    }

    private boolean checkPreviousCount(Integer previousCleanCount, Integer solrSummaryFtsCount) {
        if (previousCleanCount == null) {
            return true; // no need to check
        }
        return previousCleanCount < solrSummaryFtsCount;
    }
}
