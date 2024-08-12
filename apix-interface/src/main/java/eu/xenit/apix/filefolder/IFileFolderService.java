package eu.xenit.apix.filefolder;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.StoreRef;

/**
 * Service for operations related to files and folders.
 */
public interface IFileFolderService {

    /**
     * Get the data dictionary folder of the alfresco
     *
     * @return The noderef of the data dictionary folder
     */
    NodeRef getDataDictionary();

    /**
     * @param nodeRef The noderef of which the path is requested.
     * @return The path (from the root node in alfresco) of the node.
     */
    NodePath getPath(NodeRef nodeRef);

    /**
     * @param storeRef The store of which the root folder is requested.
     * @return The root folder of the storeRef.
     */
    NodeRef getRootFolder(StoreRef storeRef);

    /**
     * Get the company home folder of the alfresco.
     *
     * @return The noderef of the company home folder.
     */
    NodeRef getCompanyHome();

    /**
     * Checks if a folder exists.
     *
     * @param parent The parent in which the folder could be.
     * @param name The name of the folder.
     * @return Whether a folder with the given name exists in the given parent folder.
     */
    boolean existsFolder(NodeRef parent, String name);

    /**
     * Get NodeRef of node with a given parent and a given name.
     *
     * @param parent The parent of the requested node.
     * @param name The name of the requested node.
     * @return The NodeRef of node with a given parent and a given name.
     */
    NodeRef getChildNodeRef(NodeRef parent, String name);

    /**
     * Get NodeRef of node with a given parent and a given relative path.
     *
     * @param parent The parent of the requested node.
     * @param path The path from the parent node to the requested node.
     * @return The NodeRef of node with a given parent and a relative path from that parent node.
     */
    NodeRef getChildNodeRef(NodeRef parent, String[] path);

    /**
     * Create a folder within a given parent node with a given name.
     *
     * @param parent The folder in which to create a new folder.
     * @param folderName The name of the new folder.
     * @return The Noderef of the newly created folder.
     */
    NodeRef createFolder(NodeRef parent, String folderName);

    /**
     * Deletes a folder with a specific noderef.
     *
     * @param folderNodeRef The noderef of the folder that is requested to be deleted.
     */
    void deleteFolder(NodeRef folderNodeRef);


}
