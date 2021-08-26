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

    public SolrTestHelperImpl(String baseUrl, DataSource dataSource, SwitchableApplicationContextFactory searchSubSystem) {
        super(baseUrl, dataSource, searchSubSystem);
    }
}
