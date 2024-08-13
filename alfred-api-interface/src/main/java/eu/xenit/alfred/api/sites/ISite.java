package eu.xenit.alfred.api.sites;

import eu.xenit.alfred.api.data.NodeRef;
import java.util.Map;

public interface ISite {

    NodeRef getNodeRef();

    String getShortName();

    String getTitle();

    String getDescription();

    boolean isPublic();

    Map<String, NodeRef> getComponents();
}
