package eu.xenit.apix.permissions;

import eu.xenit.apix.data.NodeRef;

import java.util.Map;

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
     * getPermissionsFast is not certifiable by Alfresco because it uses the ModelDAO, which is not part of the public
     * API. However, getPermissionsFast is roughly 3 times as efficient as getPermissions. I have made case on Alfresco
     * support with case id 00930777
     *
     * @param node the noderef from which the permissions will be gathered.
     * @return Returns a map with the permissions and a PermissionValue that allows or denies actions on the provided
     * node for the current authentication.
     */
    Map<String, PermissionValue> getPermissionsFast(NodeRef node);

    /**
     * getPermissions cannot be replaced yet by getPermissionsFast because getPermissionsFast is not certifiable yet.
     * However, in the future that is what we want to do. Returns the set of permissions that are present on this
     * object. Possible values are Read, Write, Delete, CreateChildren, ReadPermissions, ChangePermissions and custom
     * permissions
     *
     * @param node the noderef from which the permissions will be gathered
     * @return Returns a map with the permissions and a PermissionValue that allows or denies actions on the provided
     * node for the current authentication
     */
    Map<String, PermissionValue> getPermissions(NodeRef node);

    void setPermission(NodeRef node, String authority, String permission);

    /**
     * Delete the permission for a given node for a given authority.
     *
     * @param node The node on which the permission is deleted.
     * @param authority The authority for which the permission is deleted.
     * @param permission The permission that is removed.
     */
    void deletePermission(NodeRef node, String authority, String permission);

    /**
     * Sets whether the given node inherits permissions from its parent.
     *
     * @param node The node to set wether it inherits permissions from its parent.
     * @param inheritPermissions Whether to inherit or not.
     */
    void setInheritParentPermissions(NodeRef node, boolean inheritPermissions);

    /**
     * returns the ACL for a given node.
     *
     * @param nodeRef the node from which the permissions will be returned.
     * @return NodePermission object informing the fact that permissions are inherited or not and which permissions are
     * directly applied to this specific node.
     */
    NodePermission getNodePermissions(NodeRef nodeRef);

    /**
     * Applies the ACL to a given node.
     *
     * @param nodeRef noderef for which the permissions will be applied.
     * @param permissions object informing the fact that permissions are inherited or not and which permissions are
     * directly applied to this specific node.
     */
    void setNodePermissions(NodeRef nodeRef, NodePermission permissions);

    /**
     * Check that the current authentication has a particular permission for the given node.
     *
     * @param nodeRef the noderef on which the permission is queried.
     * @param permission the String representation of the Permission that is being checked.
     * @return - Boolean indicating whether the asked permission is present.
     */
    boolean hasPermission(NodeRef nodeRef, String permission);

}
