package eu.xenit.alfred.api.rest.v1.sites;

import eu.xenit.alfred.api.rest.v1.nodes.NodeInfo;
import eu.xenit.alfred.api.sites.ISite;

public class SiteInfo {

    private final ISite site;

    public ISite getSite() {
        return site;
    }

    private final NodeInfo nodeInfo;

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public SiteInfo(ISite site, NodeInfo nodeInfo) {
        this.site = site;
        this.nodeInfo = nodeInfo;
    }
}
