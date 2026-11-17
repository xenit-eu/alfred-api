package eu.xenit.alfred.api.rest.v1.translation;

import eu.xenit.alfred.api.translation.ITranslationService;
import eu.xenit.alfred.api.translation.Translations;
import java.io.IOException;
import java.util.Locale;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;
import eu.xenit.alfred.api.rest.webscript.JsonResponses;

@Component("webscript.eu.xenit.alfred.api.rest.v1.translation.translations.get")
public class TranslationsWebScript extends AbstractWebScript {

    private final ITranslationService translationService;

    public TranslationsWebScript(ITranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Locale language = Locale.forLanguageTag(req.getServiceMatch().getTemplateVars().get("locale"));

        Translations translations = translationService.getTranslations(language);

        JsonResponses.writeJson(res, translations);
    }
}
