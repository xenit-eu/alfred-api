package eu.xenit.alfred.api.categories;

import eu.xenit.alfred.api.utils.PrintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Categories allow you to quickly and easily organize your content to help you retrieve the content you are looking
 * for. You classify your content items in Alfresco with categories. The categories are organized into related groups to
 * form a hierarchy. You can link a content item to more than one category.
 */
public class Category {

    private String noderef;
    private String name;
    private String qnamePath;
    private ArrayList<Category> subcategories = new ArrayList<>();

    /**
     * @return The full path of the qname of the category. An example is:
     * /app:company_home/st:sites/cm:mysite/cm:documentLibrary
     */
    public String getQnamePath() {
        return qnamePath;
    }

    /**
     * Sets the category qname path.
     *
     * @param qnamePath qname path of the category.
     */
    public void setQnamePath(String qnamePath) {
        this.qnamePath = qnamePath;
    }

    /**
     * @return The noderef of the category
     */
    public String getNoderef() {
        return noderef;
    }

    /**
     * Sets the category noderef.
     *
     * @param noderef nodeRef of the category.
     */
    public void setNoderef(String noderef) {
        this.noderef = noderef;
    }

    /**
     * @return The name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the category.
     *
     * @param name Name of the category.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The subcategories of this category.
     */
    public ArrayList<Category> getSubcategories() {
        return subcategories;
    }

    /**
     * Sets the subcategories of this category.
     *
     * @param subcategories the subcategories of the Category.
     */
    public void setSubcategories(ArrayList<Category> subcategories) {
        this.subcategories = subcategories;
    }

    @Override
    public String toString() {
        List<String> cats = new ArrayList<>();
        for (Category category : subcategories) {
            cats.add(category.toString() + "\n");
        }

        Boolean first = true;
        String subCat = "";
        for (String part : cats) {
            if (first) {
                subCat = part;
                first = false;
            } else {
                subCat += part;
            }
        }

        return "Category{" +
                "noderef='" + noderef + '\'' + '\n' +
                ", name='" + name + '\'' + '\n' +
                ", qnamePath='" + qnamePath + '\'' + '\n' +
                ", subcategories=\n" + PrintUtils.indent(">> ", subCat) +
                '}';
    }
}
