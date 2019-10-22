package eu.xenit.apix.rest.v0.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v0.RestV0Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Component("eu.xenit.apix.rest.v0.metadata.MetadataBulkWebscript")
@Authentication(AuthenticationType.USER)
public class MetadataBulkWebscript extends AbstractWebScript {

    private static final Logger logger = LoggerFactory.getLogger(MetadataBulkWebscript.class);
    @Autowired
    private INodeService service;
    @Autowired
    private IPermissionService permissionService;
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Uri(value = "/eu/xenit/metadata/bulk", method = HttpMethod.POST)
    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        ObjectMapper m = new ObjectMapper();

        JsonNode input = m.readTree(webScriptRequest.getContent().getContent());
        if (!input.isArray()) {
            throw new RuntimeException("Should be an array of noderefs");
        }

//        List<NodeRef> refs = StreamSupport.stream(input.spliterator(), false)
//                .map(x -> new NodeRef(x.asText())).collect(Collectors.toList());

        List<NodeRef> refs = new ArrayList<>(input.size());
        Iterator<JsonNode> iterator = input.elements();
        while (iterator.hasNext()) {
            refs.add(new NodeRef(iterator.next().asText()));
        }

        List<NodeMetadataV0> metadatas = new ArrayList<>();

        for (NodeRef el : refs) {
            metadatas.add(NodeMetadataV0.FromV1(service.getMetadata(el), permissionService));
        }

        ArrayNode node = m.createArrayNode();

        //node.addAll(metadatas.stream().map(data -> (JsonNode)m.valueToTree(data)).collect(Collectors.toList()));

        for (NodeMetadataV0 metadata : metadatas) {
            node.add(m.valueToTree(metadata));
        }

        String retStr = node.toString();

        webScriptResponse.setContentType("json");
        webScriptResponse.getWriter().write(retStr);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}
