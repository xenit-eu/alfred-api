package eu.xenit.apix.permissions;

import eu.xenit.apix.data.NodeRef;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface IPermissionService {

    String WRITE = "Write";
    String READ = "Read";
    String DELETE = "Delete";
    String CREATE_CHILDREN = "CreateChildren";
    String ADD_CHILDREN = "AddChildren";
    String READ_PERMISSIONS = "ReadPermissions";
    String CHANGE_PERMISSIONS = "ChangePermissions";
    String READ_RECORDS = "ReadRecords";
    String FILING = "Filing";

    /**
     * Returns the set of permissions that are present on this object.
     * Possible values are Read, Write, Delete, CreateChildren, ReadPermissions, ChangePermissions
     * and custom permissions
     * @param node The node of which the permissions are requested.
     * @return A map from permission to permission value.
     */

    /**
     * getPermissionsFast is not certifiable by Alfresco because it uses the ModelDAO, which is not part of the public
     * API. However, getPermissionsFast is roughly 3 times as efficient as getPermissions. I have made case on Alfresco
     * support with case id 00930777
     */
    Map<String, PermissionValue> getPermissionsFast(NodeRef node);

    /**
     * getPermissions cannot be replaced yet by getPermissionsFast because getPermissionsFast is not certifiable yet.
     * However, in the future that is what we want to do.
     */
    Map<String, PermissionValue> getPermissions(NodeRef node);

    public void setPermission(NodeRef node, String authority, String permission);

    /**
     * Delete the permission for a given node for a given authority.
     *
     * @param node The node on which the permission is deleted.
     * @param authority The authority for which the permission is deleted.
     * @param permission The permission that is removed.
     */
    public void deletePermission(NodeRef node, String authority, String permission);

    /**
     * Sets whether the given node inherits permissions from its parent.
     *
     * @param node The node to set wether it inherits permissions from its parent.
     * @param inheritPermissions Whether to inherit or not.
     */
    public void setInheritParentPermissions(NodeRef node, boolean inheritPermissions);

    /**
     * returns the ACL for a given node.
     *
     * @return NodePermission object informing the fact that permissions are inherited or not and which permissions are
     * directly applied to this specific node.
     */
    NodePermission getNodePermissions(NodeRef nodeRef);

    /**
     * Applies the ACL to a given node.
     *
     * @param permissions object informing the fact that permissions are inherited or not and which permissions are
     * directly applied to this specific node.
     */
    void setNodePermissions(NodeRef nodeRef, NodePermission permissions);

}
