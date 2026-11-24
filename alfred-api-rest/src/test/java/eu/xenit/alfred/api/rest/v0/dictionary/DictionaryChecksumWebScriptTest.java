package eu.xenit.alfred.api.rest.v0.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.dictionary.IDictionaryService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class DictionaryChecksumWebScriptTest {

    @Test
    void checksumIsWrappedInAnObject() throws IOException {
        IDictionaryService dictionaryService = mock(IDictionaryService.class);
        when(dictionaryService.getContentModelCheckSum()).thenReturn(123456789L);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);

        new DictionaryChecksumWebScript(dictionaryService).execute(mock(WebScriptRequest.class), res);

        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
        assertEquals("{\"checksum\":123456789}", out.toString(StandardCharsets.UTF_8));
    }
}