package eu.xenit.alfred.api.rest.v1.translation;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import eu.xenit.alfred.api.translation.ITranslationService;
import eu.xenit.alfred.api.translation.Translations;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslationsWebscript1 extends ApixV1Webscript {

    private final ITranslationService translationService;

    public TranslationsWebscript1(ITranslationService translationService) {
        this.translationService = translationService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/translations/{locale}/checksum")
    public ResponseEntity<TranslationChecksum> getChecksum(@PathVariable final String locale) {
        Locale language = Locale.forLanguageTag(locale);
        Long checksum = translationService.getTranslationsCheckSum(language);
        TranslationChecksum checksumObj = new TranslationChecksum(checksum);
        return writeJsonResponse(checksumObj);
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/translations/{locale}")
    public ResponseEntity<Translations> getTranslations(@PathVariable final String locale) {
        Locale language = Locale.forLanguageTag(locale);
        Translations translations = translationService.getTranslations(language);
        return writeJsonResponse(translations);
    }

    public static class TranslationChecksum {

        private Long checksum;

        public TranslationChecksum(Long checksum) {
            this.checksum = checksum;
        }

        public Long getChecksum() {
            return checksum;
        }

        public void setChecksum(Long checksum) {
            this.checksum = checksum;
        }
    }
}
