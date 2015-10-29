package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco42.search.configuration.FacetConfiguration;
import eu.xenit.apix.util.SolrTestHelper;
import java.io.InputStream;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchService42Test extends SearchServiceTest {

    private static final String FACET_FORMS_CONFIG_JSON = "facet-forms-config.json";
    @Autowired
    private FacetConfiguration facetConfiguration;

    @Autowired
    private FileFolderService fileFolderService;

    @Autowired
    private ContentService contentService;

    @Before
    public void Setup() {
        solrTestHelper = new SolrTestHelper("/solr", dataSource, searchSubSystem);
    }

    @Override
    public void withTestFacets(final RunAsWork<Object> work) throws Exception {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                NodeRef folder = createFolders("Data Dictionary/Fred/Forms".split("/"));
                NodeRef file = fileFolderService.searchSimple(folder, FACET_FORMS_CONFIG_JSON);
                if(file == null) {
                    file = fileFolderService.create(folder, FACET_FORMS_CONFIG_JSON, ContentModel.TYPE_CONTENT).getNodeRef();
                }

                ContentWriter contentWriter = contentService.getWriter(file, ContentModel.PROP_CONTENT, true);

                InputStream facetsConfiguration = getClass().getClassLoader().getResourceAsStream(
                        FACET_FORMS_CONFIG_JSON);

                assert facetsConfiguration != null;

                contentWriter.putContent(facetsConfiguration);

                facetConfiguration.reset();

                try {
                    work.doWork();
                } finally {
                    nodeService.removeChild(folder, file);
                    facetConfiguration.reset();
                }

                return null;
            }
        });
    }

    private NodeRef createFolders(String[] path) {

        NodeRef folder = repository.getCompanyHome();

        for (String pathName : path) {

            NodeRef childFolder = fileFolderService.searchSimple(folder, pathName);

            if(childFolder == null) {
                childFolder = fileFolderService.create(folder, pathName, ContentModel.TYPE_FOLDER).getNodeRef();
            }

            folder = childFolder;
        }

        return folder;
    }
}
