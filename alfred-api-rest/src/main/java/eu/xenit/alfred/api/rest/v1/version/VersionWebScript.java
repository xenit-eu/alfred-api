package eu.xenit.alfred.api.rest.v1.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import eu.xenit.alfred.api.version.IVersionService;
import eu.xenit.alfred.api.version.VersionDescription;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("webscript.eu.xenit.alfred.api.rest.v1.version.version.get")
public class VersionWebScript extends AbstractWebScript {

    private final ServiceRegistry serviceRegistry;
    private final IVersionService versionService;
    private final ObjectMapper objectMapper;

    public VersionWebScript(
            @Qualifier("ServiceRegistry") ServiceRegistry serviceRegistry,
            IVersionService versionService) {
        this.serviceRegistry = serviceRegistry;
        this.versionService = versionService;
        this.objectMapper = ObjectMapperFactory.getNewObjectMapper();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        VersionDescription version = transactionHelper.doInTransaction(
                versionService::getVersionDescription,
                true,
                false
        );

        // Serialize to UTF-8 bytes rather than relying on the servlet writer's default charset.
        byte[] body = objectMapper.writeValueAsBytes(version);

        res.setStatus(Status.STATUS_OK);
        res.setContentType("application/json");
        // Reproduces the header added by the original Alfresco MVC dispatcher.
        res.setHeader("Cache-Control", "no-cache");
        res.getOutputStream().write(body);
    }
}
