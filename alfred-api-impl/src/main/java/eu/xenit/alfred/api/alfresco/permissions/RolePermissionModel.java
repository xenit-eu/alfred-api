package eu.xenit.alfred.api.alfresco.permissions;

import eu.xenit.alfred.api.data.QName;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolePermissionModel {

    private final static Logger logger = LoggerFactory.getLogger(RolePermissionModel.class);
    private String roleName;
    private Set<String> actions;
    //This is the type of the node you are doing the action on (cm:content, cm:folder or st:site).
    //The allowed actions will by default be the same for all types in most cases.
    private QName type;

    public RolePermissionModel(String roleName, Set<String> actions, QName type) {
        this.roleName = roleName;
        this.actions = actions;
        this.type = type;
    }

    public RolePermissionModel(PermissionReference permissionRef, ModelDAO modelDAO, QName type) {
        if (permissionRef.getQName().equals(ContentModel.TYPE_BASE) || permissionRef.getQName()
                .equals(ContentModel.ASPECT_LOCKABLE)) {
            logger.error("permissionRef with name " + permissionRef.getName() + " is not a role");
            throw new RuntimeException("permissionRef with name " + permissionRef.getName() + " is not a role");
        }

        this.roleName = permissionRef.getName();
        this.actions = this.GetAllowedActions(permissionRef, modelDAO, type);
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<String> getActions() {
        return actions;
    }

    public QName getType() {
        return type;
    }

    private Set<String> GetAllowedActions(PermissionReference permissionRef, ModelDAO modelDAO, QName type) {
        Set<PermissionReference> granteeRefs = modelDAO.getImmediateGranteePermissions(permissionRef);
        Set<String> directPermittedActions = new HashSet<String>();
        if (modelDAO.hasFull(permissionRef)) {
            directPermittedActions.add("FullControl");
            return directPermittedActions;
        }

        for (PermissionReference granteeRef : granteeRefs) {
            if (granteeRef.getQName().equals(ContentModel.TYPE_BASE) || permissionRef.getQName()
                    .equals(ContentModel.ASPECT_LOCKABLE)) {
                directPermittedActions.add(granteeRef.getName());
            } else if (granteeRef.getQName().equals(ContentModel.TYPE_CMOBJECT)) {
                directPermittedActions.addAll(this.GetAllowedActions(granteeRef, modelDAO, type));
            }
        }
        return directPermittedActions;
    }
}
