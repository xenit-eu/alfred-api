package eu.xenit.apix.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Configurations {

    private List<ConfigurationFile> files;

    public Configurations(List<ConfigurationFile> files) {
        this.files = new ArrayList<ConfigurationFile>(files);
        Collections.sort(this.files,new ConfigurationFileComparator());
    }

    public List<ConfigurationFile> getFiles() {
        return files;
    }

    public void setFiles(List<ConfigurationFile> files) {
        this.files = files;
    }
}
