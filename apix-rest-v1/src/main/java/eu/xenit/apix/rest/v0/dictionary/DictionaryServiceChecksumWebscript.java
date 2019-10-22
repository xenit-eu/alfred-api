package eu.xenit.apix.rest.v0.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.dictionary.IDictionaryService;
import eu.xenit.apix.rest.v0.RestV0Config;
import java.io.IOException;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.rest.v0.dictionary.DictionaryServiceChecksumWebscript")
//@WebScript(baseUri = "/eu/xenit/apix/v1.1/dictionary")
@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Authentication(AuthenticationType.USER)
public class DictionaryServiceChecksumWebscript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryServiceChecksumWebscript.class);
    @Autowired
    private IDictionaryService service;
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Override
    @Uri(value = "/eu/xenit/dictionary/checksum", method = HttpMethod.GET)
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        long checksum = service.getContentModelCheckSum();

        ObjectMapper m = new ObjectMapper();

        com.fasterxml.jackson.databind.node.ObjectNode ret = m.createObjectNode();
        ret.put("checksum", checksum);
        webScriptResponse.setContentType("json");
        webScriptResponse.getWriter().write(ret.toString());
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}
