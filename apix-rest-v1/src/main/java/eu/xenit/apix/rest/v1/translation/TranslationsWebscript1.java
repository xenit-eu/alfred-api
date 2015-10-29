package eu.xenit.apix.rest.v1.translation;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import eu.xenit.apix.translation.ITranslationService;
import eu.xenit.apix.translation.Translations;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Stan on 30-Mar-16.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieve translations", value = "Translations")
@Component("eu.xenit.apix.rest.v1.translation.TranslationsWebscript1")
public class TranslationsWebscript1 extends ApixV1Webscript {

    @Autowired
    ITranslationService translationService;


    @Uri(value = "/translations/{locale}/checksum", method = HttpMethod.GET)
    @ApiOperation("Retrieve a checksum of all translations for given locale")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = TranslationChecksum.class))
    public void getChecksum(@UriVariable final String locale, WebScriptResponse response) throws IOException {
        Locale language = Locale.forLanguageTag(locale);

        Long checksum = translationService.getTranslationsCheckSum(language);

        TranslationChecksum checksumObj = new TranslationChecksum(checksum);

        writeJsonResponse(response, checksumObj);
    }

    @Uri(value = "/translations/{locale}", method = HttpMethod.GET)
    @ApiOperation("Get all available translations for given locale")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Translations.class))
    public void getTranslations(@UriVariable final String locale, WebScriptResponse response) throws IOException {
        Locale language = Locale.forLanguageTag(locale);

        Translations translations = translationService.getTranslations(language);

        writeJsonResponse(response, translations);
    }

    public static class TranslationChecksum {

        public Long checksum;

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
