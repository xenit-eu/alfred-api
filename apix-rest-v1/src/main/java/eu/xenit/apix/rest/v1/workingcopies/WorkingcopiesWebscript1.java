package eu.xenit.apix.rest.v1.workingcopies;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("eu.xenit.apix.rest.v1.WorkingcopiesWebscript1")
public class WorkingcopiesWebscript1 extends ApixV1Webscript {

    private final INodeService nodeService;

    public WorkingcopiesWebscript1(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping(value = "/v1/workingcopies")
    public ResponseEntity<?> createWorkingcopy(@RequestBody CheckoutBody checkoutBody) {
        final NodeRef originalRef = checkoutBody.getOriginal();
        NodeRef destinationRef = checkoutBody.getDestinationFolder();
        if (!nodeService.exists(originalRef)) {
            return respondDoesNotExist(originalRef);
        }

        // if a destinationRef was specified, it must exist, but nodeservice.checkout(..., null) works fine.
        if (destinationRef == null || nodeService.exists(destinationRef)) {
            NodeRef workingCopyRef = nodeService.checkout(originalRef, destinationRef);
            return writeJsonResponse(new NoderefResult(workingCopyRef));
        }

        return respondDoesNotExist(destinationRef);
    }

    @PostMapping(value = "/v1/workingcopies/{space}/{store}/{guid}/checkin")
    public ResponseEntity<?> checkinWorkingcopy(@PathVariable final String space, @PathVariable final String store,
                                   @PathVariable final String guid,
                                   @RequestBody final CheckinBody checkinBody) {
        final NodeRef nodeRef = createNodeRef(space, store, guid);
        if (nodeService.exists(nodeRef)) {
            NodeRef originalRef = nodeService.checkin(nodeRef, checkinBody.getComment(), checkinBody.getMajorVersion());
            return writeJsonResponse(new NoderefResult(originalRef));
        }
        return respondDoesNotExist(nodeRef);
    }

    @DeleteMapping(value = "/v1/workingcopies/{space}/{store}/{guid}")
    public ResponseEntity<?> cancelWorkingcopy(@PathVariable final String space, @PathVariable final String store,
            @PathVariable final String guid) {
        final NodeRef workingCopyRef = createNodeRef(space, store, guid);
        if (nodeService.exists(workingCopyRef)) {
            NodeRef originalRef = nodeService.cancelCheckout(workingCopyRef);
            return writeJsonResponse(new NoderefResult(originalRef));
        }
        return respondDoesNotExist(workingCopyRef);
    }

    @GetMapping(value = "/v1/workingcopies/{space}/{store}/{guid}/original")
    public ResponseEntity<?> getWorkingCopySource(@PathVariable final String space, @PathVariable final String store,
            @PathVariable final String guid) {
        NodeRef workingCopyRef = createNodeRef(space, store, guid);
        if (nodeService.exists(workingCopyRef)) {
            NodeRef originalRef = nodeService.getWorkingCopySource(workingCopyRef);
            return writeJsonResponse(new NoderefResult(originalRef));
        }

        return respondDoesNotExist(workingCopyRef);
    }

    private ResponseEntity<?> respondDoesNotExist(NodeRef nodeRef) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(String.format(String.format("%s does not exist.", nodeRef)));
    }
}