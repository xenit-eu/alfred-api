package eu.xenit.alfred.api.rest.v0.dictionary;

import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Returns the checksum of the content model, so clients can tell whether their cached dictionary is still current.
 */
@Component("webscript.eu.xenit.alfred.api.rest.v0.dictionary.checksum.get")
public class DictionaryChecksumWebScript extends AbstractWebScript {

    private final IDictionaryService dictionaryService;

    public DictionaryChecksumWebScript(IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        JsonResponses.writeJson(res, Map.of("checksum", dictionaryService.getContentModelCheckSum()));
    }
}