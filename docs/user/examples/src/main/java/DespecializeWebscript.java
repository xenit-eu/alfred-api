import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.dictionary.types.TypeDefinition;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.MetadataChanges;
import eu.xenit.alfred.api.node.NodeMetadata;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This webscript despecializes a node by converting it to its parent type and removing all properties that are
 * specified by its current type
 */
public class DespecializeWebscript extends DeclarativeWebScript {

    private INodeService nodeService;

    private IDictionaryService dictionaryService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        NodeRef nodeRef = new NodeRef(req.getParameter("nodeRef"));
        NodeMetadata metadata = nodeService.getMetadata(nodeRef);
        TypeDefinition nodeType = dictionaryService.GetTypeDefinition(metadata.type);
        MetadataChanges metadataChanges = new MetadataChanges();

        Map<QName, String[]> newPropertyValues = new HashMap<>();
        for (QName property : nodeType.getProperties()) {
            String[] strings = {};
            newPropertyValues.put(property, strings);
        }
        metadataChanges.setPropertiesToSet(newPropertyValues);
        metadataChanges.setType(nodeType.getParent());

        nodeService.setMetadata(nodeRef, metadataChanges);

        return Collections.emptyMap();
    }
}
