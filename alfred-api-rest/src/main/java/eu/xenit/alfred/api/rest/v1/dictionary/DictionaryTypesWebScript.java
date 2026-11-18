package eu.xenit.alfred.api.rest.v1.dictionary;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.dictionary.types.get")
public class DictionaryTypesWebScript extends AbstractWebScript {

    static final String DEFAULT_PARENT = "sys:base";

    private final IDictionaryService dictionaryService;

    public DictionaryTypesWebScript(
            @Qualifier("eu.xenit.alfred.api.dictionary.IDictionaryService") IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // Like Spring's @RequestParam(defaultValue = ...), the default also applies to an empty parameter.
        String parent = req.getParameter("parent");
        if (parent == null || parent.isEmpty()) {
            parent = DEFAULT_PARENT;
        }
        JsonResponses.writeJson(res, dictionaryService.GetSubTypeDefinitions(new QName(parent), true));
    }
}
