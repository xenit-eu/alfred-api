package eu.xenit.alfred.api.alfresco.configuration;

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.xenit.alfred.api.configuration.ConfigurationFile;
import eu.xenit.alfred.api.configuration.ConfigurationFileFlags;
import eu.xenit.alfred.api.configuration.ConfigurationService;
import eu.xenit.alfred.api.configuration.Configurations;
import eu.xenit.alfred.api.content.IContentService;
import eu.xenit.alfred.api.data.ContentInputStream;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.node.ChildParentAssociation;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.NodeMetadata;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.configuration.ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String QNAME_FOLDER = TYPE_FOLDER.toString();
    private static final QName QNAME_NAME = new QName(PROP_NAME.toString());
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    INodeService nodeService;

    @Autowired
    IContentService contentService;

    public Configurations getConfigurationFiles(String searchDirectory, String nameFilter,
            ConfigurationFileFlags requestedConfigurationFileFields) throws IOException {
        if (!searchDirectory.isEmpty() && searchDirectory.charAt(0) == '/') {
            searchDirectory = searchDirectory.substring(1);
        }
        String[] searchDirectoryParts = searchDirectory.split("/");

        // Running as system so user does not need read access on the folders in between
        NodeRef rootFolder = AuthenticationUtil.runAsSystem(() -> fileFolderService.getDataDictionary());
        logger.debug("Looking up directory {} inside datadictionary {}", searchDirectory, rootFolder);

        NodeRef searchDirectoryRef = rootFolder;
        try {
            if (!searchDirectory.isEmpty()) {
                searchDirectoryRef = fileFolderService.getChildNodeRef(rootFolder, searchDirectoryParts);
            }
        } catch (InvalidArgumentException invalidArgumentException) {
            // Wrapping exception in generic IllegalArgumentException since the original exception is an alfresco dependency,
            // which we do not want to propagate into the interface (This would be required to have the webscript declare
            // a catch clause)
            throw new IllegalArgumentException(invalidArgumentException.getMessage(), invalidArgumentException);
        }

        logger.debug("Search directory: {}", searchDirectoryRef.getValue());

        ConfigurationServiceImpl.Filter filter = new ConfigurationServiceImpl.NullFilter();

        if (nameFilter != null && !nameFilter.isEmpty()) {
            filter = new ConfigurationServiceImpl.NameFilter(nameFilter);
        }

        List<ConfigurationFile> configurationFiles = getChildrenRecursive(searchDirectoryRef, filter);

        logger.debug("Found {} configuration files: {}", configurationFiles.size(), configurationFiles);

        ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());

        for (ConfigurationFile configurationFile : configurationFiles) {
            logger.debug("Configuration file: {}", configurationFile.getNodeRef());
            if (requestedConfigurationFileFields.addContent) {
                ContentInputStream configStream = contentService.getContent(configurationFile.getNodeRef());
                if (configStream != null) {
                    BufferedReader configReader = new BufferedReader(
                            new InputStreamReader(configStream.getInputStream()));
                    StringBuilder configString = new StringBuilder(((int) configStream.getSize()));
                    String line;
                    while ((line = configReader.readLine()) != null) {
                        configString.append(line + "\n");
                    }
                    configurationFile.setContent(configString.toString());
                }
            }

            if (requestedConfigurationFileFields.addPath) {
                configurationFile.setPath(fileFolderService.getPath(configurationFile.getNodeRef()));
            }

            if (requestedConfigurationFileFields.addParsedContent) {
                ContentInputStream configStream = contentService.getContent(configurationFile.getNodeRef());
                if (configStream != null) {
                    String mimetype = configStream.getMimetype();
                    String name = configurationFile.getMetadata().getProperties().get(QNAME_NAME).get(0);
                    logger.debug("Mimetype is {}; filename is {}", mimetype, name);
                    Object parsedContent = null;
                    if (mimetype.equals("text/x-yaml") || name.endsWith(".yaml") || name.endsWith(".yml")) {
                        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                        parsedContent = yamlMapper.readValue(configStream.getInputStream(), Object.class);
                    } else if (mimetype.equals("application/json") || name.endsWith(".json")) {
                        parsedContent = jsonMapper.readValue(configStream.getInputStream(), Object.class);
                    } else {
                        logger.info("Can not parse {}", configurationFile.getNodeRef());
                    }
                    configurationFile.setParsedContent(parsedContent);
                }
            }

            if (!requestedConfigurationFileFields.addMetadata) {
                configurationFile.setMetadata(null);
            }

            if (!requestedConfigurationFileFields.addNodeRef) {
                configurationFile.setNodeRef(null);
            }

        }
        return new Configurations(configurationFiles);
    }


    private List<ConfigurationFile> getChildrenRecursive(NodeRef parent, ConfigurationServiceImpl.Filter filter) {
        List<ConfigurationFile> files = new ArrayList<>();
        List<ChildParentAssociation> childParentAssociations = nodeService.getChildAssociations(parent);
        for (ChildParentAssociation childParentAssociation : childParentAssociations) {
            NodeRef child = childParentAssociation.getTarget();
            NodeMetadata childMetadata = nodeService.getMetadata(child);
            if (childMetadata.getType().getValue().equals(QNAME_FOLDER)) {
                files.addAll(getChildrenRecursive(child, filter));
            } else if (filter.isAccepted(childMetadata)) {
                files.add(new ConfigurationFile(child, childMetadata));
            }
        }

        return files;
    }

    interface Filter {

        boolean isAccepted(NodeMetadata metadata);
    }

    class NameFilter implements ConfigurationServiceImpl.Filter {

        private Pattern filter;

        NameFilter(String nameFilter) {
            filter = Pattern.compile(nameFilter);
            logger.debug("NameFilter pattern is: {}", filter);

        }

        @Override
        public boolean isAccepted(NodeMetadata metadata) {
            String name = metadata.getProperties().get(QNAME_NAME).get(0);
            logger.debug("Checking if {} matches {}", name, filter);
            return filter.matcher(name).find();
        }

    }

    class NullFilter implements ConfigurationServiceImpl.Filter {

        @Override
        public boolean isAccepted(NodeMetadata metadata) {
            return true;
        }
    }

}
