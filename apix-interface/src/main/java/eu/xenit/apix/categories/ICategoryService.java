package eu.xenit.apix.categories;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;

import java.util.List;


/**
 * https://wiki.alfresco.com/wiki/Classification_And_Categories
 */
public interface ICategoryService {

    /**
     * Classifies a target node to a specific category.
     *
     * @param targetNode The node to classify.
     * @param categoryNode The category to which to classify the node.
     */
    void classifyNode(NodeRef targetNode, NodeRef categoryNode);


    /**
     * Retuns all categories for given classifiable aspect. This consists of a list of category roots WARNING: can be
     * slow for many categories
     *
     * @param classifiableAspectName Should be aspect of type cm:classifiable
     * @return List of categories
     */
    List<Category> getCategoryTree(QName classifiableAspectName);
}
