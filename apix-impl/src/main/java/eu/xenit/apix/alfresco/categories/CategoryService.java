package eu.xenit.apix.alfresco.categories;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.categories.Category;
import eu.xenit.apix.categories.ICategoryService;
import eu.xenit.apix.utils.java8.Optional;
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
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 30/11/2015. https://wiki.alfresco.com/wiki/Classification_And_Categories Example:
 * https://svn.alfresco.com/repos/alfresco-open-mirror/alfresco/HEAD/root/projects/repository/config/alfresco/bootstrap/categories.xml
 */

@OsgiService
@Component("eu.xenit.apix.categories.ICategoryService")
public class CategoryService implements ICategoryService {

    @Autowired
    private ApixToAlfrescoConversion c;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private org.alfresco.service.cmr.search.CategoryService categoryService;
    @Autowired
    private NamespaceService namespaceService;


    public CategoryService() {
    }


    public CategoryService(ServiceRegistry registry, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        nodeService = registry.getNodeService();
        categoryService = registry.getCategoryService();
        namespaceService = registry.getNamespaceService();

        this.c = apixToAlfrescoConversion;

    }

    /**
     * TODO, copied from https://wiki.alfresco.com/wiki/Classification_And_Categories
     */
    public void classifyNode(eu.xenit.apix.data.NodeRef targetNodeA, eu.xenit.apix.data.NodeRef categoryNodeA) {
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
     * Retuns all categories for given classifiable aspect. This consists of a list of category roots WARNING: can be
     * slow for many categories
     *
     * @param classifiableAspectNameA Should be aspect of type cm:classifiable //TODO: getCategoryTrees
     */
    public List<Category> getCategoryTree(eu.xenit.apix.data.QName classifiableAspectNameA) {
        QName classifiableAspectName = c.alfresco(classifiableAspectNameA);
        java.util.Collection<org.alfresco.service.cmr.repository.ChildAssociationRef> refs = categoryService
                .getCategories(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, classifiableAspectName,
                        org.alfresco.service.cmr.search.CategoryService.Depth.ANY);

        Collection<ChildAssociationRef> categories = categoryService
                .getClassifications(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        Optional<ChildAssociationRef> classifierRootNodeMaybe = Optional.empty();
        for (ChildAssociationRef child : categories) {
            if (!child.getQName().equals(classifiableAspectName)) {
                continue;
            }

            classifierRootNodeMaybe = Optional.of(child);
            break;
        }

//        Optional<ChildAssociationRef> classifierRootNodeMaybe = categoryService.getClassifications(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
//                .stream().filter(c -> c.getQName().equals(classifiableAspectName)).findFirst();

        if (!classifierRootNodeMaybe.isPresent()) {
            throw new RuntimeException("Aspect name does not match a classifiable");
        }
        NodeRef classifierRootNode = classifierRootNodeMaybe.get().getChildRef();

        HashMap<NodeRef, Category> map = new HashMap<>();
        List<Category> ret = new ArrayList<Category>();

        for (org.alfresco.service.cmr.repository.ChildAssociationRef r : refs) {
            NodeRef parentRef = r.getParentRef();
            NodeRef nodeRef = r.getChildRef();
            Category cat = map.get(nodeRef);
            if (cat == null) {
                cat = new Category();
                map.put(r.getChildRef(), cat);
            }
            cat.setNoderef(nodeRef.toString());
            cat.setName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
            cat.setQnamePath(nodeService.getPath(nodeRef).toPrefixString(namespaceService));

            if (r.getParentRef().equals(classifierRootNode)) {
                // Is root node
                ret.add(cat);
            } else {
                if (!map.containsKey(parentRef)) {
                    map.put(parentRef, new Category()); //Warn: not setting ref to this cat
                }
                Category parent = map.get(parentRef);
                parent.getSubcategories().add(cat);
            }
        }

        return ret;

    }
}
