package eu.xenit.alfred.api.rest.v0.categories;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.categories.Category;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClassificationGetWebscript {

    private final ICategoryService catService;

    public ClassificationGetWebscript(ICategoryService catService) {
        this.catService = catService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(
            value = "/classification/{aspectQName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Category>> execute(@PathVariable final QName aspectQName) {
        return ResponseEntity.ok(
                catService.getCategoryTree(aspectQName)
        );
    }
}
