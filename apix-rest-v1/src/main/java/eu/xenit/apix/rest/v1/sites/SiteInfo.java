package eu.xenit.apix.rest.v1.sites;

import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import eu.xenit.apix.sites.ISite;

public class SiteInfo {

    private ISite site;

    public ISite getSite() {
        return site;
    }

    private NodeInfo nodeInfo;

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public SiteInfo(ISite site, NodeInfo nodeInfo) {
        this.site = site;
        this.nodeInfo = nodeInfo;
    }
}
