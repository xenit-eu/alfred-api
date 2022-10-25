package eu.xenit.apix.rest.v1.workingcopies;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Michiel Huygen on 09/03/2016.
 */
//@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
//        description = "Access operations on working copies", value = "Workingcopies")
@RestController("eu.xenit.apix.rest.v1.WorkingcopiesWebscript1")
public class WorkingcopiesWebscript1 extends ApixV1Webscript {

    private final INodeService nodeService;

    public WorkingcopiesWebscript1(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping(value = "/v1/workingcopies")
    @ApiOperation("Checks out a new working copy for given node")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NoderefResult.class))
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

    @ApiOperation(value = "Checks in given working copy and removes it",
            notes = "Returns the noderef of the original node")
    @PostMapping(value = "/v1/workingcopies/{space}/{store}/{guid}/checkin")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NoderefResult.class),
            @ApiResponse(code = 404, message = "Not found")
    })
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

    @ApiOperation(value = "Cancels and removes a working copy", notes = "Returns the noderef of the original node")
    @DeleteMapping(value = "/v1/workingcopies/{space}/{store}/{guid}")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NoderefResult.class),
            @ApiResponse(code = 404, message = "Not found")
    })
    public ResponseEntity<?> cancelWorkingcopy(@PathVariable final String space, @PathVariable final String store,
            @PathVariable final String guid) {
        final NodeRef workingCopyRef = createNodeRef(space, store, guid);
        if (nodeService.exists(workingCopyRef)) {
            NodeRef originalRef = nodeService.cancelCheckout(workingCopyRef);
            return writeJsonResponse(new NoderefResult(originalRef));
        }
        return respondDoesNotExist(workingCopyRef);
    }

    @ApiOperation("Returns the original node for given working copy")
    @GetMapping(value = "/v1/workingcopies/{space}/{store}/{guid}/original")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = NoderefResult.class),
            @ApiResponse(code = 404, message = "Not Found")
    })
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