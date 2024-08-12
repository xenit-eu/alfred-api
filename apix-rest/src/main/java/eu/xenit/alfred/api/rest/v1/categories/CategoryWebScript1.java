package eu.xenit.alfred.api.rest.v1.categories;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.categories.Category;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController
public class CategoryWebScript1 extends ApixV1Webscript {

    private final ICategoryService categoryService;

    public CategoryWebScript1(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/category/aspect/{qname}")
    public ResponseEntity<Categories> getCategoriesForAspect(@PathVariable final String qname) {
        QName apixQName = new QName(qname);
        List<Category> categories = categoryService.getCategoryTree(apixQName);
        return writeJsonResponse(new Categories(categories));
    }
}
