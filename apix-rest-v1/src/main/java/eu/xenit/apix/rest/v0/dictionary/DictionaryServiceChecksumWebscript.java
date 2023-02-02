package eu.xenit.apix.rest.v0.dictionary;

import eu.xenit.apix.dictionary.IDictionaryService;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DictionaryServiceChecksumWebscript {

    private final IDictionaryService service;

    public DictionaryServiceChecksumWebscript(IDictionaryService service) {
        this.service = service;
    }

    @GetMapping(
            value = "/dictionary/checksum",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Long>> execute() {
        return ResponseEntity.ok(
                Collections.singletonMap(
                        "checksum",
                        service.getContentModelCheckSum()
                )
        );
    }
}
