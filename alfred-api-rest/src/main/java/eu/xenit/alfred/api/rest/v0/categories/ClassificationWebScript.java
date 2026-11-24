package eu.xenit.alfred.api.rest.v0.categories;

import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Returns the category tree of an aspect as a bare JSON array (unlike v1, which wraps it in {"categories": [...]}).
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.categories.classification.get")
public class ClassificationWebScript extends AbstractWebScript {

    private final ICategoryService categoryService;

    public ClassificationWebScript(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        QName aspectQName = new QName(req.getServiceMatch().getTemplateVars().get("aspectQName"));
        JsonResponses.writeJson(res, categoryService.getCategoryTree(aspectQName));
    }
}