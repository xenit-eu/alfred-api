package eu.xenit.apix.node;


import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.data.ContentInputStream;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.data.StoreRef;
import io.swagger.annotations.Api;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

/**
 * Service for operations on nodes.
 */
@Api(value = "/metadata", produces = "application/json")
public interface INodeService {

    /**
     * @param noderef The noderef for which the metadata is requested.
     * @return The metadata of the given noderef. In case the node has no transaction id, the transaction id is set to
     * -1. This can be the case for nodes that are stored as an old version.
     */
    NodeMetadata getMetadata(NodeRef noderef);

    /**
     * @param noderef  The noderef for which the metadata is set.
     * @param metadata The new metadata for the given noderef.
     * @return Returns the new metadata in a NodeMetadata object (contains node ref, properties, ...).
     */
    NodeMetadata setMetadata(NodeRef noderef, MetadataChanges metadata);


    /**
     * Get the root node of the given store.
     *
     * @param storeRef the Store of which the root node is requested.
     * @return the noderef of the root node.
     */
    NodeRef getRootNode(StoreRef storeRef);


    /**
     * @param ref The node for which the child associations are requested.
     * @return The child associations of the given node.
     */
    List<ChildParentAssociation> getChildAssociations(NodeRef ref);

    /**
     * @param ref The node for which the parent associations are requested.
     * @return The parent associations of the given node.
     */
    List<ChildParentAssociation> getParentAssociations(NodeRef ref);

    /**
     * Fetches all associations for which the given node is the source.
     *
     * @param ref The node for which the associations are requested.
     * @return The peer associations of the given node for which it is the source
     */
    List<NodeAssociation> getTargetAssociations(NodeRef ref);

    /**
     * Fetches all associations for which the given node is the target.
     *
     * @param ref The node for which the associations are requested.
     * @return The peer associations of the given node for which it is the target
     */
    List<NodeAssociation> getSourceAssociations(NodeRef ref);

    /**
     * Returns all target, parent and child associations of the given node.
     *
     * @param ref The node for which the associations are requested.
     * @return all target, parent and child associations of the given node.
     */
    NodeAssociations getAssociations(NodeRef ref);

    /**
     * Returns all primary ancestors of the given node.
     *
     * @param ref     The node for which the ancestors are requested.
     * @param rootRef The node up to which point the ancestors have to be retrieved.
     *                This is optional and uses Company Home as root by default.
     * @return a list of the node references of the primary parents.
     */
    List<NodeRef> getAncestors(NodeRef ref, NodeRef rootRef);

    /**
     * Creates an association between source and target of a given type.
     *
     * @param source    The source of the association to create.
     * @param target    The target of the association to create.
     * @param assocType The type of association to create.
     */
    void createAssociation(NodeRef source, NodeRef target, QName assocType);

    /**
     * Removes an association between source and target of a given type.
     *
     * @param source    The source of the association to remove.
     * @param target    The target of the association to remove.
     * @param assocType The type of association to remove.
     */
    void removeAssociation(NodeRef source, NodeRef target, QName assocType);

    /**
     * Copies a source node to a given destination parent node. Children are copied if deepCopy is true.
     *
     * @param source      The node to copy.
     * @param destination The parent which will contain the copied node.
     * @param deepCopy    If true child nodes are also copied.
     * @return The noderef of the new node.
     */
    NodeRef copyNode(NodeRef source, NodeRef destination, boolean deepCopy);

    /**
     * Moves a source node to a given destination parent node.
     *
     * @param source      The node to move.
     * @param destination The parent which will contain the moved node.
     * @return The new noderef of the moved node.
     */
    NodeRef moveNode(NodeRef source, NodeRef destination);

    /**
     * Create a node with a given parent, name and type.
     *
     * @param parent The parent node of the new node.
     * @param name   The name of the new node.
     * @param type   The type of the new node.
     * @return The noderef of the newly created node.
     */
    NodeRef createNode(NodeRef parent, String name, QName type);

    /**
     * Creation of a node giving the list of properties as well as the type. To be used when using a custom type has
     * required properties.
     *
     * @param parent      The parent node of the new node.
     * @param properties  list of properties to add to node.
     * @param type        The type of the node.
     * @param contentData can contain returned result of function createContent (or null).
     * @return The noderef of the new node.
     */
    NodeRef createNode(NodeRef parent, Map<QName, String[]> properties, QName type, ContentData contentData);

    /**
     * Delete a node.
     *
     * @param nodeRef     The noderef of the node to delete.
     * @param permanently If true the node is deleted permanently. If false the node is just moved to the trashcan.
     * @return whether the deletion was successful.
     */
    boolean deleteNode(NodeRef nodeRef, boolean permanently);

    /**
     * Delete a node by moving it to the trashcan.
     *
     * @param nodeRef The noderef of the node to delete.
     * @return whether the deletion was successful.
     */
    boolean deleteNode(NodeRef nodeRef);

    /**
     * @param nodeRef The noderef of a node that could exist.
     * @return Whether a noderef exists.
     */
    boolean exists(NodeRef nodeRef);

    /**
     * Checks out the given node placing a working copy in the destination specified.
     *
     * @param original    The node to checkout
     * @param destination The destination in which the working copy is placed.
     * @return null in case the checkout fails.
     */
    NodeRef checkout(NodeRef original, NodeRef destination);

    /**
     * Checks in the given working copy.
     *
     * @param nodeRef      the node that will be checked.
     * @param comment      Give a comment while checking in.
     * @param majorVersion If true this version will have an increased major version number relative to the previous
     *                     commit. Otherwise an increased minor version number.
     * @return the noderef to the original node, updated with the checked in state.
     */
    NodeRef checkin(NodeRef nodeRef, String comment, boolean majorVersion);

    /**
     * Cancels a checkout of a working copy. Returns the noderef of the original node.
     *
     * @param nodeRef The working copy of which the checkout will be canceled.
     * @return The noderef of the original node.
     */
    NodeRef cancelCheckout(NodeRef nodeRef);

    /**
     * @param nodeRef The noderef of the working copy.
     * @return The Noderef of the source of the working copy (The original node).
     */
    NodeRef getWorkingCopySource(NodeRef nodeRef);

    /**
     * Sets content of a specific node. Also changes mimetype by guessing it using content and original file name.
     *
     * @param node             noderef of the node where the content of the inputStream will placed.
     * @param inputStream      The input stream that contains the content. In case the input stream is null the content will
     *                         be set to empty. The inputstream will be read (in combination with originalFilename)
     *                         to make a best effort guess about which mimetype the content should have.
     *                         To enforce a specific mimetype, use {@link #setContent(NodeRef, ContentData) setContent(NodeRef, ContentData)}
     *                         with a ContentData object that has the mimetype explicitly set.
     *                         A ContentData object can be obtained from the method {@link #createContent(InputStream, String, String) createContent}
     * @param originalFilename The filename of the content. This is only used to guess the mimetype of the node.
     */
    void setContent(eu.xenit.apix.data.NodeRef node, InputStream inputStream, String originalFilename);

    /**
     * Set the content of a node.
     *
     * @param node        The node of which the content is set.
     * @param contentData The content to set.
     */
    void setContent(eu.xenit.apix.data.NodeRef node, eu.xenit.apix.data.ContentData contentData);


    /**
     * Creates a content without linking it to a specific node yet.
     *
     * @param inputStream Can be null: in that case, the content property of the file is removed.
     * @param mimeType    ex: "application/pdf"
     * @param encoding    ex: "UTF-8", ....
     * @return the URL of the content (can be set to the content property of a node).
     */
    ContentData createContent(InputStream inputStream, String mimeType, String encoding);

    /**
     * Creates a content without linking it to a specific node yet.
     *
     * @param inputStream Can be null: in that case, the content property of the file is removed.
     * @param fileName    Needed for guessing the mimetype. (Does necessary not need to contain the extension
     *                    as mimeTypeGuessing will also work on content of the inputStream)
     * @param encoding    Encoding of the inputStream. ex: "UTF-8", ....
     * @return the URL of the content (can be set to the content property of a node).
     */
    ContentData createContentWithMimetypeGuess(InputStream inputStream, String fileName, String encoding);

    /**
     * Returns content as inputStream + other related informations (mimeType, size, ...)
     *
     * @param node the noderef from which the content will be gathered.
     * @return ContentInputStream.inputStream has to be closed!
     */
    ContentInputStream getContent(eu.xenit.apix.data.NodeRef node);

    /**
     * Extracts metadata on a node
     *
     * @param node the noderef from which metadata will be extracted.
     */
    void extractMetadata(eu.xenit.apix.data.NodeRef node);


}
