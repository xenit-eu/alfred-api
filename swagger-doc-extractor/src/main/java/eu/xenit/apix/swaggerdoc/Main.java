package eu.xenit.apix.swaggerdoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.apix.alfresco.version.VersionService;
import io.swagger.util.Json;

public class Main {

    public static void main(String args[]) throws JsonProcessingException {
        System.out.println(generateSwagger());
    }

    private static String generateSwagger() throws JsonProcessingException {
        DocumentationWebscript documentationWebscript = new DocumentationWebscript(new VersionService(), null);
        return Json.mapper().writeValueAsString(documentationWebscript.generateSwagger());
    }

}