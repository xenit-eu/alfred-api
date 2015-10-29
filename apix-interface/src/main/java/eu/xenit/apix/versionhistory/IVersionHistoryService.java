package eu.xenit.apix.versionhistory;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Versioning information for a node
 */
public interface IVersionHistoryService {

    /**
     * @param nodeRef The node for which the versionhistory is requested.
     * @return The versionhistory of the given node.
     */
    public VersionHistory GetVersionHistory(NodeRef nodeRef);

    /**
     * Make sure versioning is enabled for the specified nodeRef
     *
     * @param nodeRef the nodeRef for which we want to enable versioning
     */
    public void ensureVersioningEnabled(NodeRef nodeRef, Map<QName, Serializable> versionProperties);

    /**
     * Create a new version for the specified nodeRef
     */
    public void createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties);

    /**
     * Delete the version history for the specified nodeRef
     *
     * @param nodeRef the node for which to delete the version history
     */
    public void deleteVersionHistory(NodeRef nodeRef);

    /**
     * Delete a specific version associated with a node reference.
     * <p>
     * This operation is permanent, the specific version in the version history is deleted and cannot be retrieved.
     * <p>
     * If this is the last version, then the current version label for the node reference is reset and any subsequent
     * versions of the node will result in a new version history being created.
     *
     * @param nodeRef the node reference
     * @param versionLabel the label of the version to delete
     */
    public void deleteVersion(NodeRef nodeRef, String versionLabel);

    /**
     * @param nodeRef the node for which the root version is requested.
     * @return The root version of the given node
     */
    public Version getRootVersion(NodeRef nodeRef);

    /**
     * @param nodeRef the node for which the head version is requested.
     * @return The head version of the given node
     */
    public Version getHeadVersion(NodeRef nodeRef);

    /**
     * Revert given node to the version specified by tag
     */
    public void revert(NodeRef nodeRef, String versionLabel);
}
