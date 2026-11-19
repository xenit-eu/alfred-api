package eu.xenit.alfred.api.rest.v1.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.translation.ITranslationService;
import eu.xenit.alfred.api.translation.Translations;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class TranslationWebScriptsTest {

    private final ITranslationService translationService = mock(ITranslationService.class);

    @Test
    void translationsUseTheLocaleFromTheUrl() throws IOException {
        Locale dutchBelgium = Locale.forLanguageTag("nl-BE");
        when(translationService.getTranslations(dutchBelgium))
                .thenReturn(new Translations(List.of(), List.of(), List.of(), List.of()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        new TranslationsWebScript(translationService).execute(requestWithLocale("nl-BE"), res);

        verify(translationService).getTranslations(dutchBelgium);
        assertJsonOk(res);
        JsonNode body = new ObjectMapper().readTree(out.toString(StandardCharsets.UTF_8));
        assertEquals(List.of("aspects", "association", "properties", "types"), sortedFieldNames(body));
    }

    @Test
    void checksumIsWrappedInAnObject() throws IOException {
        when(translationService.getTranslationsCheckSum(Locale.ENGLISH)).thenReturn(123456789L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        new TranslationChecksumWebScript(translationService).execute(requestWithLocale("en"), res);

        assertJsonOk(res);
        assertEquals("{\"checksum\":123456789}", out.toString(StandardCharsets.UTF_8));
    }

    private static void assertJsonOk(WebScriptResponse res) {
        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
    }

    private static List<String> sortedFieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        names.sort(null);
        return names;
    }

    private static WebScriptRequest requestWithLocale(String locale) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/v1/translations/{locale}", Map.of("locale", locale),
                "/apix/v1/translations/"));
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}