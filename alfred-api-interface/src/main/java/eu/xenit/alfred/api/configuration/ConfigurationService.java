package eu.xenit.alfred.api.configuration;

import java.io.IOException;

public interface ConfigurationService {

    Configurations getConfigurationFiles(
            String searchDirectory,
            String nameFilter,
            ConfigurationFileFlags configurationFileFlags) throws IOException;
}
