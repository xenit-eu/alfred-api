package eu.xenit.alfred.api.rest.v1.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import eu.xenit.alfred.api.translation.ITranslationService;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.translation.translations-checksum.get")
public class TranslationChecksumWebScript extends AbstractWebScript {

    private final ITranslationService translationService;
    private final ObjectMapper objectMapper;

    public TranslationChecksumWebScript(ITranslationService translationService) {
        this.translationService = translationService;
        this.objectMapper = ObjectMapperFactory.getNewObjectMapper();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Locale language = Locale.forLanguageTag(req.getServiceMatch().getTemplateVars().get("locale"));

        Map<String, Long> checksum = Map.of("checksum", translationService.getTranslationsCheckSum(language));

        // Serialize to UTF-8 bytes rather than relying on the servlet writer's default charset.
        byte[] body = objectMapper.writeValueAsBytes(checksum);

        res.setStatus(Status.STATUS_OK);
        res.setContentType("application/json");
        // Reproduces the header added by the original Alfresco MVC dispatcher.
        res.setHeader("Cache-Control", "no-cache");
        res.getOutputStream().write(body);
    }
}