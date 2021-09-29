package eu.xenit.apix.util;

import javax.sql.DataSource;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should only be used by the integration tests.
 */
public class SolrTestHelperImpl extends SolrTestHelperBaseImpl {
    private Logger logger = LoggerFactory.getLogger(SolrTestHelperImpl.class);

    private final String SOLR_SUMMARY_FTS_NEW = "Node count with FTSStatus New";
    private final String SOLR_SUMMARY_FTS_DIRTY = "Node count with FTSStatus Dirty";
    private final String SOLR_SUMMARY_FTS_CLEAN = "Node count with FTSStatus Clean";

    public SolrTestHelperImpl(String baseUrl, DataSource dataSource, SwitchableApplicationContextFactory searchSubSystem) {
        super(baseUrl, dataSource, searchSubSystem);
    }

    @Override
    public int getNumberOfFtsStatusCleanDocs() {
        return (Integer) super.getSummaryFtsSection().get(SOLR_SUMMARY_FTS_CLEAN);
    }

    @Override
    public boolean isContentIndexed() {
        JSONObject ftsBlock = super.getSummaryFtsSection();
        logger.debug("solrSummaryFTSBlock: {}", ftsBlock.toString(4));
        return 0 == ((Integer) ftsBlock.get(SOLR_SUMMARY_FTS_NEW)) && 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY);
    }

    @Override
    public boolean isContentIndexed(int previousCleanCount) {
        JSONObject ftsBlock = super.getSummaryFtsSection();
        logger.debug("solrSummaryFTSBlock: {}", ftsBlock.toString(4));
        return 0 == ((Integer) ftsBlock.get(SOLR_SUMMARY_FTS_NEW))
                && 0 == (Integer) ftsBlock.get(SOLR_SUMMARY_FTS_DIRTY)
                && previousCleanCount < ((Integer) ftsBlock.get(SOLR_SUMMARY_FTS_CLEAN));
    }
}
