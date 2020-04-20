package eu.xenit.apix.sites;

import eu.xenit.apix.data.NodeRef;
import java.util.Map;

public interface ISite {

    NodeRef getNodeRef();

    String getShortName();

    String getTitle();

    String getDescription();

    boolean isPublic();

    Map<String, NodeRef> getComponents();
}
