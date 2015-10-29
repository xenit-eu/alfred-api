package eu.xenit.apix.rest.v1.temp;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.Api;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;


/**
 * Created by Michiel Huygen on 03/05/2016.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Display logs")
@Api(hidden = true)
@Component("eu.xenit.apix.rest.v1.temp.LogsWebscript")
public class LogsWebscript extends ApixV1Webscript {

    @Uri(value = "/tmp/log", defaultFormat = "text")
    @Authentication(AuthenticationType.ADMIN)
    public void showLog(@RequestParam(defaultValue = "200") int lines, WebScriptResponse resp) throws IOException {
        ArrayList<String> output = new ArrayList<String>();

        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(
                new File("/opt/alfresco/tomcat/logs/catalina.out"))) {
            for (int i = 0; i < lines; i++) {
                output.add(reader.readLine() + "\n");
            }
        }

        Writer writer = resp.getWriter();

        for (int i = lines - 1; i >= 0; i--) {
            writer.append(output.get(i));
        }


    }
}
