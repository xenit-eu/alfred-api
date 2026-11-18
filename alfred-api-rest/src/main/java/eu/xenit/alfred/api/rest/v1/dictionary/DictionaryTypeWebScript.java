package eu.xenit.alfred.api.rest.v1.dictionary;

import eu.xenit.alfred.api.dictionary.IDictionaryService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.dictionary.type.get")
public class DictionaryTypeWebScript extends AbstractWebScript {

    private final IDictionaryService dictionaryService;

    public DictionaryTypeWebScript(
            @Qualifier("eu.xenit.alfred.api.dictionary.IDictionaryService") IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        DictionaryResponses.writeDefinitionOrNotFound(res,
                dictionaryService.GetTypeDefinition(DictionaryResponses.qnameFrom(req)));
    }
}
