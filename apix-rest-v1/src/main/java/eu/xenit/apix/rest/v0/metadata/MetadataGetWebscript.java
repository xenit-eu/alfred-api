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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Component("eu.xenit.apix.rest.v0.metadata.MetadataGetWebscript")
@Authentication(AuthenticationType.USER)
public class MetadataGetWebscript extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(MetadataGetWebscript.class);
    @Autowired
    private INodeService service;
    @Autowired
    private IPermissionService permissionService;
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Uri(value = "/eu/xenit/metadata", method = HttpMethod.GET)
    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
//"workspace://SpacesStore/7d45cebd-a8b3-4ef2-b2b6-ca584082f47c"

        ObjectMapper m = new ObjectMapper();

        NodeRef noderef = new NodeRef(webScriptRequest.getParameter("noderef"));

        NodeMetadata metadata = service.getMetadata(noderef);

        String retStr = m.writeValueAsString(NodeMetadataV0.FromV1(metadata, permissionService));

        webScriptResponse.setContentType("json");
        webScriptResponse.getWriter().write(retStr);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}
