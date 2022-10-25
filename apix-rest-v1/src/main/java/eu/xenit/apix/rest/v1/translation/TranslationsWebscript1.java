package eu.xenit.apix.rest.v1.translation;

import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.translation.ITranslationService;
import eu.xenit.apix.translation.Translations;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Stan on 30-Mar-16.
 */
//@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
//        description = "Retrieve translations", value = "Translations")
@RestController("eu.xenit.apix.rest.v1.translation.TranslationsWebscript1")
public class TranslationsWebscript1 extends ApixV1Webscript {

    private final ITranslationService translationService;

    public TranslationsWebscript1(ITranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping(value = "/v1/translations/{locale}/checksum")
    @ApiOperation("Retrieve a checksum of all translations for given locale")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = TranslationChecksum.class))
    public ResponseEntity<TranslationChecksum> getChecksum(@PathVariable final String locale) {
        Locale language = Locale.forLanguageTag(locale);
        Long checksum = translationService.getTranslationsCheckSum(language);
        TranslationChecksum checksumObj = new TranslationChecksum(checksum);
        return writeJsonResponse(checksumObj);
    }

    @GetMapping(value = "/v1/translations/{locale}")
    @ApiOperation("Get all available translations for given locale")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Translations.class))
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
