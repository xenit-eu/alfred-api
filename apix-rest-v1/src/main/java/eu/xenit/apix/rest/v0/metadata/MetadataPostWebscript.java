package eu.xenit.apix.rest.v0.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.node.NodeMetadata;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v0.RestV0Config;
import java.io.IOException;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Component("eu.xenit.apix.rest.v0.metadata.MetadataPostWebscript")
@Authentication(AuthenticationType.USER)
public class MetadataPostWebscript extends AbstractWebScript {

    @Autowired
    private INodeService service;
    @Autowired
    private IPermissionService permissionService;
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Uri(value = "/eu/xenit/metadata", method = HttpMethod.POST)
    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        ObjectMapper m = new ObjectMapper();
        NodeRef noderef = new NodeRef(webScriptRequest.getParameter("noderef"));
        MetadataChangesV0 changes = m.readValue(
                webScriptRequest.getContent().getInputStream(), MetadataChangesV0.class);

        NodeMetadata metadata = service.setMetadata(noderef, changes.ToV1());
        String retStr = m.writeValueAsString(NodeMetadataV0.FromV1(metadata, permissionService));
        webScriptResponse.setContentType("json");
        webScriptResponse.getWriter().write(retStr);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
