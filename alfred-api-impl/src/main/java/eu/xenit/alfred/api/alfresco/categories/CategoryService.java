package eu.xenit.alfred.api.alfresco.categories;

import com.google.common.collect.Iterables;
import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.categories.Category;
import eu.xenit.alfred.api.categories.ICategoryService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.categories.ICategoryService")
public class CategoryService implements ICategoryService {

    @Autowired
    private AlfredApiToAlfrescoConversion c;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private org.alfresco.service.cmr.search.CategoryService categoryService;
    @Autowired
    private NamespaceService namespaceService;

    public CategoryService() {
    }

    public CategoryService(ServiceRegistry registry, AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion) {
        nodeService = registry.getNodeService();
        categoryService = registry.getCategoryService();
        namespaceService = registry.getNamespaceService();
        c = alfredApiToAlfrescoConversion;
    }

    /**
     * TODO, copied from https://wiki.alfresco.com/wiki/Classification_And_Categories
     */
    public void classifyNode(
            eu.xenit.alfred.api.data.NodeRef targetNodeA, eu.xenit.alfred.api.data.NodeRef categoryNodeA) {
        NodeRef targetNode = c.alfresco(targetNodeA);
        NodeRef categoryNode = c.alfresco(categoryNodeA);
        // Replace any existing aspects
        ArrayList<NodeRef> categories = new ArrayList<NodeRef>(1);
        categories.add(categoryNode);
        if (!nodeService.hasAspect(targetNode, ContentModel.ASPECT_GEN_CLASSIFIABLE)) {
            HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_CATEGORIES, categories);
            nodeService.addAspect(targetNode, ContentModel.ASPECT_GEN_CLASSIFIABLE, props);
        } else {
            nodeService.setProperty(targetNode, ContentModel.PROP_CATEGORIES, categories);
        }
    }

    /**
     * Returns all categories for given classifiable aspect. This consists of a list of category roots.
     *
     * WARNING: Can be slow for many categories.
     *
     * @param classifiableAspectNameA Should be aspect of type cm:classifiable
     */
    public List<Category> getCategoryTree(eu.xenit.alfred.api.data.QName classifiableAspectNameA) {
        QName classifiableAspectName = c.alfresco(classifiableAspectNameA);
        NodeRef classifierRootNode = getClassificationRootNode(classifiableAspectName);

        HashMap<NodeRef, Category> categoryLookup = new HashMap<>();
        List<Category> result = new ArrayList<>();

        // Walk through categories and build up category tree
        for (org.alfresco.service.cmr.repository.ChildAssociationRef r : categoryService.getCategories(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                classifiableAspectName,
                org.alfresco.service.cmr.search.CategoryService.Depth.ANY)) {
            NodeRef parentRef = r.getParentRef();
            NodeRef nodeRef = r.getChildRef();
            Category category = categoryLookup.get(nodeRef);
            if (category == null) {
                category = new Category();
                categoryLookup.put(r.getChildRef(), category);
            }
            category.setNoderef(nodeRef.toString());
            category.setName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
            category.setQnamePath(nodeService.getPath(nodeRef).toPrefixString(namespaceService));

            if (r.getParentRef().equals(classifierRootNode)) {
                // Is root node
                result.add(category);
            } else {
                if (!categoryLookup.containsKey(parentRef)) {
                    categoryLookup.put(parentRef,
                            new Category()); // Only empty category, which is filled in a later iteration
                }
                Category parent = categoryLookup.get(parentRef);
                parent.getSubcategories().add(category);
            }
        }
        return result;
    }

    private NodeRef getClassificationRootNode(QName classifiableAspectName) {
        // We use this unintuitive way of getting the category root, since categoryService.getClassifications() can
        // take minutes at customers having a large SOLR.
        Collection<ChildAssociationRef> categories = categoryService.getRootCategories(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                classifiableAspectName);
        if (categories.isEmpty()) {
            throw new RuntimeException("Aspect name does not match a classifiable");
        }
        // What we actually get back is the first level categories UNDER the category root
        // Thus, we return one of their parents.
        return Iterables.get(categories, 0).getParentRef();
    }

}
