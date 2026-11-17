package eu.xenit.alfred.api.rest.v1.categories;

import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.categories.categories.get")
public class CategoriesWebScript extends AbstractWebScript {

    private final ICategoryService categoryService;

    public CategoriesWebScript(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        QName alfredApiQName = new QName(req.getServiceMatch().getTemplateVars().get("qname"));

        Categories categories = new Categories(categoryService.getCategoryTree(alfredApiQName));

        JsonResponses.writeJson(res, categories);
    }
}
