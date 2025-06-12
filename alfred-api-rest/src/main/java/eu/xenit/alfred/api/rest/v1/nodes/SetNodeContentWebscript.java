package eu.xenit.alfred.api.rest.v1.nodes;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("webscript.eu.xenit.alfred.api.rest.v1.nodes.set-content.put")
public class SetNodeContentWebscript extends AbstractWebScript {
    private final static Logger logger = LoggerFactory.getLogger(SetNodeContentWebscript.class);


    private final ServiceRegistry serviceRegistry;

    private final INodeService nodeService;

    public SetNodeContentWebscript(@Qualifier("ServiceRegistry") ServiceRegistry serviceRegistry,
                                   INodeService nodeService) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = nodeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String space = req.getServiceMatch().getTemplateVars().get("space");
        String store = req.getServiceMatch().getTemplateVars().get("store");
        String guid = req.getServiceMatch().getTemplateVars().get("guid");

        NodeRef finalDestination = new NodeRef(space, store, guid);

        FormData.FormField file = getFile((FormData) req.parseContent());

        try {
            RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
            transactionHelper.doInTransaction(() -> {
                nodeService
                        .setContent(finalDestination, file.getInputStream(), file.getFilename());
                return null;
            }, false, true);
            res.setStatus(Status.STATUS_OK);
        } catch (AccessDeniedException e) {
            logger.debug("Not Authorized", e);
            res.setStatus(Status.STATUS_FORBIDDEN);
            res.getWriter().write("Not authorised to execute this operation");
        }
    }

    private FormData.FormField getFile(FormData formData) {
        if (!formData.hasField("file")) {
            throw new IllegalArgumentException("no 'file' field provided");
        }
        for (FormData.FormField field : formData.getFields()) {
            if ("file".equals(field.getName()) && field.getIsFile()) {
                return field;
            }
        }
        throw new IllegalArgumentException("'file' field was not a file");
    }

}
