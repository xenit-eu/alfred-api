package eu.xenit.alfred.api.alfresco.filefolder;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.StoreRef;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.filefolder.NodePath;
import eu.xenit.alfred.api.utils.StringUtils;
import java.util.Arrays;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.filefolder.IFileFolderService")
public class FileFolderService implements IFileFolderService {

    private final static Logger logger = LoggerFactory.getLogger(FileFolderService.class);
    private static final String DEFAULT_PROTOCOL = "workspace";
    private static final String DEFAULT_STORE = "SpacesStore";
    private AlfredApiToAlfrescoConversion c;
    private NodeService nodeService;
    private PermissionService permissionService;
    private NamespaceService nameSpaceService;
    private org.alfresco.service.cmr.model.FileFolderService fileFolderService;

    @Autowired
    public FileFolderService(ServiceRegistry serviceRegistry,
            AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion) {
        this.nodeService = serviceRegistry.getNodeService();
        this.nameSpaceService = serviceRegistry.getNamespaceService();
        this.permissionService = serviceRegistry.getPermissionService();
        this.fileFolderService = serviceRegistry.getFileFolderService();

        this.c = alfredApiToAlfrescoConversion;
    }

    @Override
    public NodeRef getCompanyHome() {
        StoreRef storeRef = new StoreRef(DEFAULT_PROTOCOL, DEFAULT_STORE);
        NodeRef rootNodeRef = this.getRootFolder(storeRef);

        org.alfresco.service.namespace.QName qname = org.alfresco.service.namespace.QName
                .createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home");
        List<ChildAssociationRef> assocRefs = nodeService
                .getChildAssocs(c.alfresco(rootNodeRef), ContentModel.ASSOC_CHILDREN, qname);
        return c.alfredApi(assocRefs.get(0).getChildRef());
    }

    @Override
    public NodeRef getDataDictionary() {
        org.alfresco.service.cmr.repository.NodeRef companyHome = c.alfresco(getCompanyHome());

        QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary");
        List<ChildAssociationRef> assocRefs = nodeService
                .getChildAssocs(companyHome, ContentModel.ASSOC_CONTAINS, qname);
        return c.alfredApi(assocRefs.get(0).getChildRef());
    }

    @Override
    public NodePath getPath(NodeRef nodeRef) {
        Path path = this.nodeService.getPath(c.alfresco(nodeRef));

        NodePath ret = new NodePath();
        ret.setDisplayPath(path.toDisplayPath(this.nodeService, this.permissionService));
        ret.setQnamePath(path.toPrefixString(this.nameSpaceService));

        return ret;
    }

    @Override
    public NodeRef getRootFolder(StoreRef storeRef) {
        org.alfresco.service.cmr.repository.NodeRef result = this.nodeService.getRootNode(c.alfresco(storeRef));
        return c.alfredApi(result);
    }

    @Override
    public boolean existsFolder(NodeRef parent, String name) {
        try {
            this.fileFolderService.resolveNamePath(c.alfresco(parent), Arrays.asList(name));
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @Override
    public NodeRef getChildNodeRef(NodeRef parent, String name) {
        try {
            FileInfo result = this.fileFolderService.resolveNamePath(c.alfresco(parent), Arrays.asList(name));
            return c.alfredApi(result.getNodeRef());
        } catch (FileNotFoundException e) {
            throw new InvalidArgumentException(String.format("Folder '%s' doesn't exist!", name));
        }
    }

    @Override
    public NodeRef getChildNodeRef(NodeRef parent, String[] path) {
        try {
            FileInfo result = this.fileFolderService.resolveNamePath(c.alfresco(parent), Arrays.asList(path));
            return c.alfredApi(result.getNodeRef());
        } catch (FileNotFoundException e) {
            throw new InvalidArgumentException(
                    String.format("Folder '%s' doesn't exist!", StringUtils.join("/", path)));
        }
    }

    @Override
    public NodeRef createFolder(NodeRef parent, String folderName) {
        FileInfo result = this.fileFolderService.create(c.alfresco(parent), folderName, ContentModel.TYPE_FOLDER);
        return c.alfredApi(result.getNodeRef());
    }

    @Override
    public void deleteFolder(NodeRef folderNodeRef) {
        this.fileFolderService.delete(c.alfresco(folderNodeRef));
    }

    private org.alfresco.service.cmr.repository.NodeRef nodeExistingInDestination(
            org.alfresco.service.cmr.repository.NodeRef parentRef, String name) {
        List<ChildAssociationRef> childAssocs = this.nodeService.getChildAssocs(parentRef);
        for (ChildAssociationRef childAssoc : childAssocs) {
            org.alfresco.service.cmr.repository.NodeRef childRef = childAssoc.getChildRef();
            String childName = (String) this.nodeService.getProperty(childRef, ContentModel.PROP_NAME);
            if (childName.equals(name)) {
                return childRef;
            }
        }
        return null;
    }
}
