package eu.xenit.apix.rest.v1.workingcopies;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.INodeService;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 09/03/2016.
 */
@WebScript(baseUri = RestV1Config.BaseUrl, families = RestV1Config.Family, defaultFormat = "json",
        description = "Access operations on working copies", value = "Workingcopies")
@Component("eu.xenit.apix.rest.v1.WorkingcopiesWebscript1")
public class WorkingcopiesWebscript1 extends ApixV1Webscript {

    private final static Logger logger = LoggerFactory.getLogger(WorkingcopiesWebscript1.class);

    @Autowired
    INodeService nodeService;

    @Autowired
    IPermissionService permissionService;

    @Autowired
    IFileFolderService fileFolderService;

    @Autowired
    ServiceRegistry serviceRegistry;

    @Uri(value = "/workingcopies", method = HttpMethod.POST)
    @ApiOperation("Checks out a new working copy for given node")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NoderefResult.class))
    public void createWorkingcopy(CheckoutBody checkoutBody, WebScriptResponse response) throws IOException {
        final NodeRef originalRef = checkoutBody.getOriginal();
        NodeRef destinationRef = checkoutBody.getDestinationFolder();

        final NodeRef finalDestinationRef = destinationRef;
        NodeRef workingCopyRef = nodeService.checkout(originalRef, finalDestinationRef);

//        NodeInfo nodeInfo = this.nodeRefToNodeInfo(workingCopyRef, this.fileFolderService, this.nodeService, this.permissionService);

        writeJsonResponse(response, new NoderefResult(workingCopyRef));
    }

//    @Uri(value = "/workingcopies/{space}/{store}/{guid}", method = HttpMethod.GET)
//    @ApiOperation("Checks out a new working copy for given node")
//    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NodeInfo.class))
//    public void getWorkingcopy(@UriVariable final String space, @UriVariable final String store, @UriVariable final String guid,
//                         WebScriptResponse response) throws IOException {
//        final NodeRef originalRef = this.createNodeRef(space, store, guid);
//
//        Object resultObject = serviceRegistry.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
//            @Override
//            public Object execute() throws Throwable {
//                NodeRef workingCopyRef = nodeService.checkout(originalRef, null);
//                return workingCopyRef;
//            }
//        }, false, true);
//
//        NodeRef workingCopyRef = new NodeRef(resultObject.toString());
//
//        NodeInfo nodeInfo = this.nodeRefToNodeInfo(workingCopyRef, this.fileFolderService, this.nodeService, this.permissionService);
//
//        writeJsonResponse(response, nodeInfo);
//    }

    @ApiOperation(value = "Checks in given working copy and removes it", notes = "Returns the noderef of the original node")
    @Uri(value = "/workingcopies/{space}/{store}/{guid}/checkin", method = HttpMethod.POST)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NoderefResult.class))
    public void checkinWorkingcopy(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid,
            final CheckinBody checkinBody, WebScriptResponse response) throws IOException {
        final NodeRef nodeRef = this.createNodeRef(space, store, guid);

        NodeRef originalRef = nodeService.checkin(nodeRef, checkinBody.getComment(), checkinBody.getMajorVersion());

        writeJsonResponse(response, new NoderefResult(originalRef));
    }

    @ApiOperation(value = "Cancels and removes a working copy", notes = "Returns the noderef of the original node")
    @Uri(value = "/workingcopies/{space}/{store}/{guid}", method = HttpMethod.DELETE)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NoderefResult.class))
    public void cancelWorkingcopy(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid,
            WebScriptResponse response) throws IOException {
        final NodeRef workingCopyRef = this.createNodeRef(space, store, guid);

        NodeRef originalRef = nodeService.cancelCheckout(workingCopyRef);

        writeJsonResponse(response, new NoderefResult(originalRef));
    }

    @ApiOperation("Returns the original node for given working copy")
    @Uri(value = "/workingcopies/{space}/{store}/{guid}/original", method = HttpMethod.GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = NoderefResult.class))
    public void getWorkingCopySource(@UriVariable final String space, @UriVariable final String store,
            @UriVariable final String guid,
            WebScriptResponse response) throws IOException {
        NodeRef workingCopyRef = this.createNodeRef(space, store, guid);
        NodeRef originalRef = this.nodeService.getWorkingCopySource(workingCopyRef);

        writeJsonResponse(response, new NoderefResult(originalRef));
    }

}