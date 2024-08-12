package eu.xenit.alfred.api.rest.v1.temp;

import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import eu.xenit.alfred.api.WIP.IWIPService;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.rest.v1.ApixV1Webscript;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("eu.xenit.alfred.api.rest.v1.temp.WIPWebscript")
public class WIPWebscript extends ApixV1Webscript {

    private final IWIPService wipService;

    public WIPWebscript(IWIPService wipService) {
        this.wipService = wipService;
    }

    @AlfrescoTransaction(readOnly = true)
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/content/previews/pdf")
    public ResponseEntity<?> getPreviewPdf(@PathVariable String space,
            @PathVariable String store,
            @PathVariable String guid) {
        final NodeRef nodeRef = new NodeRef(space, store, guid);
        //TODO: from /searchapp/download
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
