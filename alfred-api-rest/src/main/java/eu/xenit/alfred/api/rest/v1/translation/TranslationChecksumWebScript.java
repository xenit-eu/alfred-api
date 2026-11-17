package eu.xenit.alfred.api.rest.v1.translation;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.translation.ITranslationService;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.translation.translations-checksum.get")
public class TranslationChecksumWebScript extends AbstractWebScript {

    private final ITranslationService translationService;

    public TranslationChecksumWebScript(ITranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Locale language = Locale.forLanguageTag(req.getServiceMatch().getTemplateVars().get("locale"));
        Map<String, Long> checksum = Map.of("checksum", translationService.getTranslationsCheckSum(language));
        JsonResponses.writeJson(res, checksum);
    }
}