package eu.xenit.alfred.api.alfresco.versionhistory;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.versionhistory.IVersionHistoryService;
import eu.xenit.alfred.api.versionhistory.Version;
import eu.xenit.alfred.api.versionhistory.VersionHistory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.versionhistory.VersionHistoryService")
public class VersionHistoryService implements IVersionHistoryService {

    private AlfredApiToAlfrescoConversion c;
    private org.alfresco.service.cmr.version.VersionService alfrescoVersionHistoryService;
    private NodeService nodeService;

    @Autowired
    public VersionHistoryService(org.alfresco.service.cmr.version.VersionService versionService,
            AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion, NodeService nodeService) {
        this.alfrescoVersionHistoryService = versionService;
        this.nodeService = nodeService;
        this.c = alfredApiToAlfrescoConversion;
    }

    @Override
    public VersionHistory GetVersionHistory(NodeRef nodeRef) {
        org.alfresco.service.cmr.version.VersionHistory v = alfrescoVersionHistoryService
                .getVersionHistory(c.alfresco(nodeRef));
        if (v == null) //If no versionhistory, no versionhistory is returned.
        {
            return null;
        }
        Collection<org.alfresco.service.cmr.version.Version> versions = v.getAllVersions();
        List<Version> vList = new ArrayList<Version>();
        for (org.alfresco.service.cmr.version.Version version : versions) {
            vList.add(VersionAlfrescoToAlfredApi(version));
        }
        return new VersionHistory(vList);
    }

    private Version VersionAlfrescoToAlfredApi(org.alfresco.service.cmr.version.Version version) {
        String description = version.getDescription();
        String versionLabel = version.getVersionLabel();
        String modifier = version.getFrozenModifier();
        Date modified = version.getFrozenModifiedDate();
        Version.VersionType vType =
                version.getVersionType() == VersionType.MAJOR ?
                        Version.VersionType.MAJOR :
                        (version.getVersionType() == VersionType.MINOR ?
                                Version.VersionType.MINOR :
                                Version.VersionType.UNKNOWN);

        Version ret =
                new Version(modifier, modified, versionLabel, description, vType,
                        c.alfredApi(version.getFrozenStateNodeRef()));
        return ret;
    }

    @Override
    public void ensureVersioningEnabled(NodeRef nodeRef, Map<QName, Serializable> versionProperties) {
        HashMap<org.alfresco.service.namespace.QName, Serializable> parz = new HashMap<org.alfresco.service.namespace.QName, Serializable>();
        for (QName qn : versionProperties.keySet()) {
            parz.put(c.alfresco(qn), versionProperties.get(qn));
        }
        alfrescoVersionHistoryService.ensureVersioningEnabled(c.alfresco(nodeRef), parz);
    }

    @Override
    public void createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties) {
        alfrescoVersionHistoryService.createVersion(c.alfresco(nodeRef), versionProperties);
    }

    @Override
    public void deleteVersionHistory(NodeRef nodeRef) {
        alfrescoVersionHistoryService.deleteVersionHistory(c.alfresco(nodeRef));
    }

    @Override
    public void deleteVersion(NodeRef nodeRef, String versionLabel) {
        org.alfresco.service.cmr.repository.NodeRef alfNode = c.alfresco(nodeRef);
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService.getVersionHistory(alfNode)
                .getVersion(versionLabel);
        alfrescoVersionHistoryService.deleteVersion(alfNode, alfVersion);
    }

    @Override
    public Version getRootVersion(NodeRef nodeRef) {
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService
                .getVersionHistory(c.alfresco(nodeRef)).getRootVersion();
        return VersionAlfrescoToAlfredApi(alfVersion);
    }

    @Override
    public Version getHeadVersion(NodeRef nodeRef) {
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService
                .getVersionHistory(c.alfresco(nodeRef)).getHeadVersion();
        return VersionAlfrescoToAlfredApi(alfVersion);
    }

    @Override
    public void revert(NodeRef nodeRef, String versionLabel) {
        org.alfresco.service.cmr.repository.NodeRef alfNode = c.alfresco(nodeRef);
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService.getVersionHistory(alfNode)
                .getVersion(versionLabel);
        alfrescoVersionHistoryService.revert(alfNode, alfVersion, false);
    }
}


