package eu.xenit.apix.rest.v1.temp;

import eu.xenit.apix.WIP.IWIPService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("eu.xenit.apix.rest.v1.temp.WIPWebscript")
public class WIPWebscript extends ApixV1Webscript {

    private final IWIPService wipService;

    public WIPWebscript(IWIPService wipService) {
        this.wipService = wipService;
    }

    @ApiOperation(value = "Downloads preview file for given node")
    @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/content/previews/pdf")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    @ApiImplicitParams({@ApiImplicitParam(name = "file", paramType = "form", dataType = "file", required = true)})
    public ResponseEntity<?> getPreviewPdf(@PathVariable String space,
                              @PathVariable String store,
                              @PathVariable String guid) {
        final NodeRef nodeRef = new NodeRef(space, store, guid);
        //TODO: from /searchapp/download
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
