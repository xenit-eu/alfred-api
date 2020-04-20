package eu.xenit.apix.alfresco.sites;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.sites.ISite;
import java.util.Map;

public class Site implements ISite {

    private NodeRef nodeRef;

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    private String shortName;

    public String getShortName() {
        return shortName;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    private boolean isPublic;

    public boolean isPublic() {
        return isPublic;
    }

    private Map<String, NodeRef> components;

    public Map<String, NodeRef> getComponents() {
        return components;
    }

    public Site(NodeRef nodeRef, String shortName, String title, String description, boolean isPublic,
            Map<String, NodeRef> components) {
        this.nodeRef = nodeRef;
        this.shortName = shortName;
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.components = components;
    }
}
