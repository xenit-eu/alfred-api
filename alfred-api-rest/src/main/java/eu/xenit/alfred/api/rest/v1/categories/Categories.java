package eu.xenit.alfred.api.rest.v1.categories;

import eu.xenit.alfred.api.categories.Category;
import java.util.List;

public class Categories {

    private List<Category> categories;

    public Categories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
