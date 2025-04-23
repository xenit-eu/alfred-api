package eu.xenit.apix.bootstrap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


/**
Class to create a node at startup with a human readable name, that is consistent accross restarts.
Intended to be used in debugging sessions by humans. Automated tests should not rely upon this and create their own nodes.

Creation can be enabled/disabled with the global property `eu.xenit.apix.integrationtest.bootstrap.enabled`. Behaviour of parseBoolean should ensure this defaults to false if property not set.

Class is located in IntegrationTest project as to not be included in released artifacts
*/
@Component
public class Bootstrap implements InitializingBean {

    public static final String WELL_KNOWN_TESTNODE_NAME = "well-known-testnode";
    private static final String ENABLED_GLOBAL_PROPERTIES_KEY = "eu.xenit.apix.integrationtest.bootstrap.enabled";

    private RetryingTransactionHelper transactionHelper;
    private NodeLocatorService nodeLocator;
    private NodeService nodeService;
    private Properties globalProperties;

    @Autowired
    public Bootstrap(
            RetryingTransactionHelper retryingTransactionHelper,
            NodeLocatorService nodeLocatorService,
            NodeService nodeService,
            @Qualifier("global-properties") Properties globalProperties
            ) {
        this.transactionHelper = retryingTransactionHelper;
        this.nodeLocator = nodeLocatorService;
        this.nodeService = nodeService;
        this.globalProperties = globalProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Boolean.parseBoolean(globalProperties.getProperty(ENABLED_GLOBAL_PROPERTIES_KEY))) {
            AuthenticationUtil.runAsSystem(
                    () -> transactionHelper.doInTransaction(
                            () -> {
                                NodeRef companyHome = nodeLocator.getNode(CompanyHomeNodeLocator.NAME, null, null);
                                NodeRef wellKnownTestNode = nodeService.getChildByName(companyHome,
                                        ContentModel.ASSOC_CONTAINS, WELL_KNOWN_TESTNODE_NAME);
                                if (wellKnownTestNode == null) {
                                    Map<QName, Serializable> folderProperties = new HashMap<>();
                                    folderProperties.put(ContentModel.PROP_NAME, WELL_KNOWN_TESTNODE_NAME);
                                    folderProperties.put(ContentModel.PROP_NODE_UUID, WELL_KNOWN_TESTNODE_NAME);
                                    nodeService.createNode(
                                            companyHome,
                                            ContentModel.ASSOC_CONTAINS,
                                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                    WELL_KNOWN_TESTNODE_NAME),
                                            ContentModel.TYPE_FOLDER,
                                            folderProperties
                                    );
                                }
                                return null;
                            },
                            false,
                            true
                    )
            );
        }
    }
}
