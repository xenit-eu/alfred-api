package eu.xenit.alfred.api.rest.v1.categories;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.categories.categories.get")
public class CategoriesWebScript extends AbstractWebScript {

    private final ICategoryService categoryService;
    private final ObjectMapper objectMapper;

    public CategoriesWebScript(ICategoryService categoryService) {
        this.categoryService = categoryService;
        this.objectMapper = ObjectMapperFactory.getNewObjectMapper();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        QName alfredApiQName = new QName(req.getServiceMatch().getTemplateVars().get("qname"));

        Categories categories = new Categories(categoryService.getCategoryTree(alfredApiQName));

        // Serialize to UTF-8 bytes rather than relying on the servlet writer's default charset.
        byte[] body = objectMapper.writeValueAsBytes(categories);

        res.setStatus(Status.STATUS_OK);
        res.setContentType("application/json");
        // Reproduces the header added by the original Alfresco MVC dispatcher.
        res.setHeader("Cache-Control", "no-cache");
        res.getOutputStream().write(body);
    }
}
