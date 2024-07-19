package eu.xenit.apix.tests.categories;

import eu.xenit.apix.categories.Category;
import eu.xenit.apix.categories.ICategoryService;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.tests.JavaApiBaseTest;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michiel Huygen on 27/11/2015.
 */
public class CategoryServiceTest extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(CategoryServiceTest.class);
    private static final String ADMIN_USER_NAME = "admin";

    private QName generalClassifiableQName;
    private ICategoryService categoryService;

    public CategoryServiceTest(){
        categoryService = getBean(ICategoryService.class);
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        generalClassifiableQName = c.apix(ContentModel.ASPECT_GEN_CLASSIFIABLE);
    }

    /**
     * Test for development purposes
     */
//    @Test
//    @Ignore // This is a manual test
//    public void TestGetCategoryInfo() {
//        java.util.Collection<QName> aspects = serviceRegistry.getCategoryService().getClassificationAspects();
//
//        logger.debug(">>> Classification aspects: ");
//        for (QName a : aspects) {
//            logger.debug(a.toString());
//        }
//        logger.debug(">>> RootCategories for classifiable ==> this is weird");
//        java.util.Collection<org.alfresco.service.cmr.repository.ChildAssociationRef> cats = serviceRegistry.getCategoryService().getRootCategories(new StoreRef("workspace", "SpacesStore"), QName.createQName("cm", "classifiable"));
//        for (org.alfresco.service.cmr.repository.ChildAssociationRef c : cats) {
//            logger.debug(c.toString());
//        }
//
//        for (QName a : aspects) {
//            logger.debug("> All categories for " + a + " aspect, depth 10");
//            java.util.Collection<org.alfresco.service.cmr.repository.ChildAssociationRef> allCats =
//                    serviceRegistry.getCategoryService().getCategories(new StoreRef("workspace", "SpacesStore"), a, CategoryService.Depth.ANY);
//
//            for (org.alfresco.service.cmr.repository.ChildAssociationRef c : allCats) {
//                logger.debug(String.format("Qname: %s   TypeName: %s", c.getQName(), c.getTypeQName()));
//            }
//        }
//
//        logger.debug(">>> Classifications");
//        for (org.alfresco.service.cmr.repository.ChildAssociationRef cls : serviceRegistry.getCategoryService().getClassifications(new StoreRef("workspace", "SpacesStore"))) {
//            logger.debug(cls.toString());
//        }
//
//
//        QName asp = aspects.stream().skip(1).findFirst().get();
//
//        logger.debug(">>> RootCategories: " + asp);
//        java.util.Collection<org.alfresco.service.cmr.repository.ChildAssociationRef> cats2 = serviceRegistry.getCategoryService().getRootCategories(new StoreRef("workspace", "SpacesStore"), asp);
//        for (org.alfresco.service.cmr.repository.ChildAssociationRef c : cats2) {
//            logger.debug(c.toString());
//        }
//    }
    @Test
    public void TestGetCategoryTree() {
        List<Category> roots = categoryService.getCategoryTree(generalClassifiableQName);

        Assert.assertTrue("Has languages in roots", matchAnyCategory(roots, "Languages"));
//        assertTrue("Has languages in roots", roots.stream().anyMatch(t -> t.getName().equals("Languages")));
//        assertTrue("Has regions", roots.stream().filter(t -> t.getName().equals("Regions"))
//                .findFirst()
//                .map(c -> c.getSubcategories().size() > 0)
//                .get());

        List<Category> filtered = filterCategories(roots, "Regions");
        Assert.assertFalse(filtered.isEmpty());

        Category category = filtered.get(0);

        Assert.assertFalse(category.getSubcategories().isEmpty());


    }

    @Test
    public void TestGetCategoryTree_qnamepath() {

        java.util.List<Category> roots = categoryService.getCategoryTree(generalClassifiableQName);

        List<Category> categories = filterCategories(roots, "Languages");
        Assert.assertFalse(categories.isEmpty());

        Category category = categories.get(0);
        String qnamePath = category.getQnamePath();
        Assert.assertEquals("/cm:categoryRoot/cm:generalclassifiable/cm:Languages", qnamePath);

//        assertEquals("/cm:categoryRoot/cm:generalclassifiable/cm:Languages",
//                roots.stream().filter(t -> t.getName().equals("Languages"))
//                        .findFirst().map(c -> c.getQnamePath()).get());
        /*assertEquals("/{http://www.alfresco.org/model/content/1.0}categoryRoot/"
                +"{http://www.alfresco.org/model/content/1.0}generalclassifiable/"
                +"{http://www.alfresco.org/model/content/1.0}Regions",
                roots.stream().filter(t -> t.getName().equals("Regions"))
                .findFirst()
                .map(c -> c.getSubcategories().stream().findFirst().get())
                .map(c -> c.getQnamePath())
                .get());*/

    }

    @Test
    public void TestGetSubcategories() {

        java.util.List<Category> roots = categoryService.getCategoryTree(generalClassifiableQName);

        Assert.assertTrue("Has languages in roots", matchAnyCategory(roots, "Languages"));
//        assertTrue("Has languages in roots", roots.stream().anyMatch(t -> t.getName().equals("Languages")));
//        assertTrue("Has regions", roots.stream().filter(t -> t.getName().equals("Regions"))
//                .findFirst()
//                .map(c -> c.getSubcategories().size() > 0)
//                .get());

        List<Category> filtered = filterCategories(roots, "Regions");
        Assert.assertFalse(filtered.isEmpty());

        Category category = filtered.get(0);

        Assert.assertFalse(category.getSubcategories().isEmpty());
    }

    private Boolean matchAnyCategory(List<Category> categories, String compare) {
        for (Category category : categories) {
            if (category.getName().equals(compare)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private List<Category> filterCategories(List<Category> unfiltered, String filter) {
        List<Category> result = new ArrayList<>(unfiltered.size());

        for (Category original : unfiltered) {
            if (original.getName().equals(filter)) {
                result.add(original);
            }
        }

        return result;
    }

}
