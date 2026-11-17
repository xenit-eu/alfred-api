package eu.xenit.alfred.api.rest.v1.version;

import eu.xenit.alfred.api.rest.webscript.JsonResponses;
import eu.xenit.alfred.api.version.IVersionService;
import eu.xenit.alfred.api.version.VersionDescription;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("webscript.eu.xenit.alfred.api.rest.v1.version.version.get")
public class VersionWebScript extends AbstractWebScript {

    private final ServiceRegistry serviceRegistry;
    private final IVersionService versionService;

    public VersionWebScript(
            @Qualifier("ServiceRegistry") ServiceRegistry serviceRegistry,
            IVersionService versionService) {
        this.serviceRegistry = serviceRegistry;
        this.versionService = versionService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        VersionDescription version = transactionHelper.doInTransaction(
                versionService::getVersionDescription,
                true,
                false
        );

        JsonResponses.writeJson(res, version);
    }
}
