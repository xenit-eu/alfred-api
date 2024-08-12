package eu.xenit.alfred.api.configuration;

import eu.xenit.alfred.api.filefolder.NodePath;

import java.util.Comparator;

public class ConfigurationFileComparator implements Comparator<ConfigurationFile> {

    private String buildString(ConfigurationFile toString) {
        //The reason multiple different spaces are returned is: being more deterministic.
        if (toString == null) {
            return "";
        }
        NodePath path = toString.getPath();
        if (path == null) {
            return " ";
        }
        String displayPath = path.getDisplayPath();
        if (displayPath == null) {
            return "  ";
        }
        return displayPath;
    }

    @Override
    public int compare(ConfigurationFile first, ConfigurationFile second) {
        return buildString(first).compareTo(buildString(second));
    }
}
