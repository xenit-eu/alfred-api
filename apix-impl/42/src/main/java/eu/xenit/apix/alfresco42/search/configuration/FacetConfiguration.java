package eu.xenit.apix.alfresco42.search.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Stan on 19-Feb-16.
 */
public class FacetConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(FacetConfiguration.class);

    private FileFolderService fileFolderService;
    private Repository repository;
    private ContentService contentService;
    private final static String FRED_FORMS_CONFIG_FILE = "Data Dictionary/Fred/Forms/facet-forms-config.json";
    private NodeRef customConfigNode = null;
    public FacetConfiguration() {
    }
    public FacetConfiguration(ServiceRegistry serviceRegistry, Repository repository) {
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.repository = repository;
        this.contentService = serviceRegistry.getContentService();
    }

    public void reset() {
        customConfigNode = null;
    }

    private NodeRef getFredConfigFile() {
        if (customConfigNode != null) {
            return customConfigNode;
        }

        try {
            customConfigNode = this.fileFolderService
                    .resolveNamePath(repository.getCompanyHome(), Arrays.asList(FRED_FORMS_CONFIG_FILE.split("/"))).getNodeRef();
        } catch (FileNotFoundException fnfe) {
            logger.warn("Unable to find config file " + FRED_FORMS_CONFIG_FILE);
            // crap
            return null;
        }

        return customConfigNode;
    }

    private InputStream getConfigNodeContent() {
        NodeRef configNode = this.getFredConfigFile();

        if (configNode == null) {
            return null;
        }

        ContentReader reader = this.contentService.getReader(configNode, ContentModel.PROP_CONTENT);

        return reader.getContentInputStream();
    }

    public List<String> getFacetConfig() {
        InputStream config = getConfigNodeContent();
        if (config != null) {
            return this.getFacetConfig(config);
        } else {
            return this.getDefaultConfig();
        }

    }

    private List<String> getDefaultConfig() {
        List<String> facets = new ArrayList<>();

        facets.add("@" + ContentModel.PROP_MODIFIER.toString() + ".__.u");
        facets.add("@{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}" + "content.mimetype");
        facets.add("@" + ContentModel.PROP_CATEGORIES.toString());
        facets.add("TYPE");

        return facets;
    }

    public List<String> getFacetConfig(InputStream config) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(config);

            for (int i = 0; i < tree.size(); i++) {
                if (tree.get(i).get("Id").asText().equals("search")) {
                    JsonNode node = tree.get(i).get("Forms").get(0).get("sets").get(0).get("Fields");

                    List<String> facets = new ArrayList<>();
                    for (int j = 0; j < node.size(); j++) {
                        facets.add(node.get(j).get("Id").asText());
                    }

                    return facets;

                }
            }

            return Collections.emptyList();
        } catch (IOException ioe) {
            // crap!
            logger.error("Error while getting facet configuration", ioe);
            return Collections.emptyList();
        }

    }

}
