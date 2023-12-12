package eu.xenit.apix.configuration;

import java.util.ArrayList;
import java.util.List;

public class Configurations {

    private List<ConfigurationFile> files;

    public Configurations() {
    }

    public Configurations(List<ConfigurationFile> files) {
        this.files = new ArrayList<>(files);
        this.files.sort(new ConfigurationFileComparator());
    }

    public List<ConfigurationFile> getFiles() {
        return files;
    }

    public void setFiles(List<ConfigurationFile> files) {
        this.files = files;
    }
}
