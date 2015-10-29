package eu.xenit.apix.alfresco.versionhistory;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@OsgiService
@Service("eu.xenit.apix.versionhistory.VersionHistoryService")

public class VersionHistoryService implements IVersionHistoryService {

    private ApixToAlfrescoConversion c;
    private org.alfresco.service.cmr.version.VersionService alfrescoVersionHistoryService;
    private NodeService nodeService;

    @Autowired
    public VersionHistoryService(org.alfresco.service.cmr.version.VersionService versionService,
            ApixToAlfrescoConversion apixToAlfrescoConversion, NodeService nodeService) {
        this.alfrescoVersionHistoryService = versionService;
        this.nodeService = nodeService;
        this.c = apixToAlfrescoConversion;
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
            vList.add(VersionAlfrescoToApix(version));
        }
        return new VersionHistory(vList);
    }

    private Version VersionAlfrescoToApix(org.alfresco.service.cmr.version.Version version) {
        String description = version.getDescription();
        String versionLabel = version.getVersionLabel();
        String modifier = version.getFrozenModifier();
        Date modified = version.getFrozenModifiedDate();
        eu.xenit.apix.versionhistory.Version.VersionType vType =
                version.getVersionType() == VersionType.MAJOR ?
                        eu.xenit.apix.versionhistory.Version.VersionType.MAJOR :
                        (version.getVersionType() == VersionType.MINOR ?
                                eu.xenit.apix.versionhistory.Version.VersionType.MINOR :
                                eu.xenit.apix.versionhistory.Version.VersionType.UNKNOWN);

        eu.xenit.apix.versionhistory.Version ret =
                new Version(modifier, modified, versionLabel, description, vType,
                        c.apix(version.getFrozenStateNodeRef()));
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
        return VersionAlfrescoToApix(alfVersion);
    }

    @Override
    public Version getHeadVersion(NodeRef nodeRef) {
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService
                .getVersionHistory(c.alfresco(nodeRef)).getHeadVersion();
        return VersionAlfrescoToApix(alfVersion);
    }

    @Override
    public void revert(NodeRef nodeRef, String versionLabel) {
        org.alfresco.service.cmr.repository.NodeRef alfNode = c.alfresco(nodeRef);
        org.alfresco.service.cmr.version.Version alfVersion = alfrescoVersionHistoryService.getVersionHistory(alfNode)
                .getVersion(versionLabel);
        alfrescoVersionHistoryService.revert(alfNode, alfVersion, false);
    }
}


