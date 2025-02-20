package eu.xenit.alfred.api.rest.v1.temp;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import java.io.File;
import java.io.IOException;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LogsWebscript extends AlfredApiV1Webscript {

    private final String logPath;

    public LogsWebscript(Environment env) {
        logPath = env.resolvePlaceholders("$CATALINA_HOME/logs/catalina.out");
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/tmp/log", produces = {MediaType.TEXT_PLAIN_VALUE})
    @AlfrescoAuthentication(AuthenticationType.ADMIN)
    public ResponseEntity<String> showLog(@RequestParam(defaultValue = "200") int lines) throws IOException {
        StringBuilder log = new StringBuilder();
        File logFile = new File(logPath);
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile)) {
            for (int i = 0; i < lines; i++) {
                log.append(reader.readLine())
                        .append("\n");
            }
        }
        return ResponseEntity.ok(log.toString());
    }
}
