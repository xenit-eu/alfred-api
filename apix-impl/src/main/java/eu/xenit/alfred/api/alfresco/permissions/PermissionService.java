package eu.xenit.alfred.api.alfresco.permissions;

import eu.xenit.alfred.api.alfresco.ApixToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.permissions.NodePermission;
import eu.xenit.alfred.api.permissions.NodePermission.Access;
import eu.xenit.alfred.api.permissions.PermissionValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by kenneth on 10.03.16.
 */
@Service("eu.xenit.alfred.api.permissions.IPermissionService")
public class PermissionService implements IPermissionService {

    private final static Logger logger = LoggerFactory.getLogger(PermissionService.class);
    private final static String FULL_CONTROL = "FullControl";
    private static final Set<String> ALL_REGISTERED_PERMISSIONS = new HashSet<>(20);
    private final String[] permissionStrings = new String[]{
            READ,
            WRITE,
            DELETE,
            CREATE_CHILDREN,
            ADD_CHILDREN,
            READ_PERMISSIONS,
            CHANGE_PERMISSIONS,
            READ_RECORDS,
            FILING
    };
    @Autowired
    public Repository repository;
    @Autowired
    public ModelDAO modelDAO;
    private org.alfresco.service.cmr.security.PermissionService permissionService;
    private ApixToAlfrescoConversion c;
    private Map<String, PermissionValue> fullControlPermissions = new HashMap<String, PermissionValue>();

    @Autowired
    public PermissionService(ServiceRegistry serviceRegistry, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        this.permissionService = serviceRegistry.getPermissionService();
        for (String permissionString : this.permissionStrings) {
            this.fullControlPermissions.put(permissionString, PermissionValue.ALLOW);
        }
        this.c = apixToAlfrescoConversion;
    }

    private synchronized Set<String> getAllRegisteredPermissions() {
        if (ALL_REGISTERED_PERMISSIONS.isEmpty()) {
            Set<PermissionReference> allPermissions = modelDAO.getAllPermissions();
            for (PermissionReference pr : allPermissions) {
                ALL_REGISTERED_PERMISSIONS.add(pr.getName());
            }
        }
        return ALL_REGISTERED_PERMISSIONS;
    }

    private List<AccessPermission> sorted(Set<AccessPermission> perms) {
        List<AccessPermission> permsSorted = new ArrayList<>(perms);
        Collections.sort(permsSorted, new Comparator<AccessPermission>() {
            @Override
            public int compare(AccessPermission left, AccessPermission right) {
                return left.getAuthority().compareTo(right.getAuthority());
            }
        });
        return permsSorted;
    }


    @Override
    public NodePermission getNodePermissions(NodeRef nodeRef) {
        NodePermission result = new NodePermission();
        org.alfresco.service.cmr.repository.NodeRef nr = c.alfresco(nodeRef);

        boolean inherit = this.permissionService.getInheritParentPermissions(nr);
        result.setInheritFromParent(inherit);

        Set<AccessPermission> allPerms = this.permissionService.getAllSetPermissions(nr);

        List<Access> accessList = new ArrayList<>();
        for (AccessPermission perm : sorted(allPerms)) {
            if (perm.getPosition() == 0) {
                Access access = new NodePermission.Access();
                access.setAllowed(perm.getAccessStatus() == AccessStatus.ALLOWED);
                access.setAuthority(perm.getAuthority());
                access.setPermission(perm.getPermission());
                accessList.add(access);
            }
        }
        result.setOwnAccessList(new HashSet<Access>(accessList));

        accessList = new ArrayList<>();
        for (AccessPermission perm : sorted(allPerms)) {
            if (perm.getPosition() != 0) {
                Access access = new NodePermission.Access();
                access.setAllowed(perm.getAccessStatus() == AccessStatus.ALLOWED);
                access.setAuthority(perm.getAuthority());
                access.setPermission(perm.getPermission());
                accessList.add(access);
            }
        }
        result.setInheritedAccessList(new HashSet<Access>(accessList));

        return result;
    }

    @Override
    public void setNodePermissions(NodeRef nodeRef, NodePermission permissions) {
        if (permissions.getInheritedAccessList() != null && permissions.getInheritedAccessList().size() > 0) {
            throw new IllegalArgumentException(
                    "Only ownAccessList is allowed when setting ACL (not inheritedAccessList).");
        }

        org.alfresco.service.cmr.repository.NodeRef nr = c.alfresco(nodeRef);

        this.permissionService.deletePermissions(nr);
        this.permissionService.setInheritParentPermissions(nr, permissions.isInheritFromParent());

        // Apply new ones.
        for (Access access : permissions.getOwnAccessList()) {
            this.permissionService.setPermission(nr, access.getAuthority(), access.getPermission(), access.isAllowed());
        }
    }

    @Override
    public boolean hasPermission(NodeRef nodeRef, String permission) {
        AccessStatus accessStatus = permissionService.hasPermission(c.alfresco(nodeRef), permission);
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        switch (accessStatus) {
            case ALLOWED:
                logger.debug("User {} has permission {} on node {}", fullyAuthenticatedUser, permission, nodeRef);
                return true;
            case DENIED:
            case UNDETERMINED:
            default:
                logger.debug("User {} does not have permission {} on node {} due to access status {}",
                        fullyAuthenticatedUser, permission, nodeRef, accessStatus);
                return false;
        }
    }


    @Override
    public void setPermission(NodeRef node, String authority, String permission) {
        permissionService.setPermission(c.alfresco(node), authority, permission, true);

    }

    @Override
    public void deletePermission(NodeRef node, String authority, String permission) {
        permissionService.deletePermission(c.alfresco(node), authority, permission);
    }

    @Override
    public void setInheritParentPermissions(NodeRef node, boolean inheritPermissions) {
        permissionService.setInheritParentPermissions(c.alfresco(node), inheritPermissions);
    }

    @Override
    public Map<String, PermissionValue> getPermissionsFast(NodeRef node) {
        org.alfresco.service.cmr.repository.NodeRef alfrescoNoderef = c.alfresco(node);

        AccessStatus canReadPermissions = this.permissionService.hasPermission(alfrescoNoderef, READ_PERMISSIONS);
        if (!canReadPermissions.equals(AccessStatus.ALLOWED)) {
            return this.getPermissions(node);
        }

        final List<RolePermissionModel> rolePermissionModels = new ArrayList<RolePermissionModel>();
        final List<String> fullControlRoles = new ArrayList<String>();
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                Set<PermissionReference> permissionRefs = modelDAO.getAllExposedPermissions();
                for (PermissionReference permissionRef : permissionRefs) {
                    logger.debug("======================================================");
                    logger.debug("permission ref name: " + permissionRef.getName());
                    logger.debug("permission ref qname: " + permissionRef.getQName().getPrefixString());

                    if (permissionRef.getQName().equals(ContentModel.TYPE_BASE)) {
                        continue;
                    }

                    RolePermissionModel rolePermissionModel = new RolePermissionModel(permissionRef, modelDAO,
                            new QName("{http://www.alfresco.org/model/content/1.0}content"));
                    if (rolePermissionModel.getActions().contains(FULL_CONTROL)) {
                        fullControlRoles.add(rolePermissionModel.getRoleName());
                    }
                    rolePermissionModels.add(rolePermissionModel);
                }

                return null;
            }
        });

        HashSet<String> allowedActions = new HashSet<String>();
        Set<AccessPermission> accessPermissions = permissionService.getPermissions(alfrescoNoderef);
        if (this.hasFullControl(fullControlRoles, accessPermissions)) {
            return this.fullControlPermissions;
        }
        for (AccessPermission accessPermission : accessPermissions) {
            logger.debug("accessPermission authority: " + accessPermission.getAuthority());
            logger.debug("accessPermission permission: " + accessPermission.getPermission());
            logger.debug("accessPermission status: " + accessPermission.getAccessStatus());

            if (accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)) {
                for (RolePermissionModel rolePermissionModel : rolePermissionModels) {
                    if (rolePermissionModel.getRoleName().equals(accessPermission.getPermission())) {
                        Set<String> actions = rolePermissionModel.getActions();
                        allowedActions.addAll(actions);
                        break;
                    }
                }
            }
        }

        Map<String, PermissionValue> resultPermissions = new HashMap<>();
        for (String permissionString : permissionStrings) {
            if (allowedActions.contains(permissionString)) {
                resultPermissions.put(permissionString, PermissionValue.ALLOW);
            } else {
                resultPermissions.put(permissionString, PermissionValue.DENY);
            }
        }

        return resultPermissions;
    }

    @Override
    public Map<String, PermissionValue> getPermissions(NodeRef node) {
        Map<String, PermissionValue> result = new HashMap<>();

        for (String permString : permissionStrings) {
            if (!getAllRegisteredPermissions().contains(permString)) {
                result.put(permString, PermissionValue.DENY);
                continue;
            }
            AccessStatus accessStatus = this.permissionService.hasPermission(c.alfresco(node), permString);
            // Translate from Alfresco enums to apix enums
            switch (accessStatus) {
                case ALLOWED:
                    result.put(permString, PermissionValue.ALLOW);
                    break;
                case DENIED:
                case UNDETERMINED:
                    result.put(permString, PermissionValue.DENY);
                    break;
            }
        }
        return result;
    }

    private boolean hasFullControl(List<String> fullControlRoles, Set<AccessPermission> accessPermissions) {
        for (AccessPermission accessPermission : accessPermissions) {
            if (accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED) && fullControlRoles
                    .contains(accessPermission.getPermission())) {
                return true;
            }
        }
        return false;
    }
}
