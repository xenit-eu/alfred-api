package eu.xenit.alfred.api.rest.v1.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import eu.xenit.alfred.api.exceptions.FileExistsException;
import eu.xenit.alfred.api.filefolder.IFileFolderService;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.node.MetadataChanges;
import eu.xenit.alfred.api.permissions.IPermissionService;
import eu.xenit.alfred.api.rest.jackson.ObjectMapperFactory;
import eu.xenit.alfred.api.rest.v1.AlfredApiV1Webscript;
import java.io.IOException;
import java.io.InputStream;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.stereotype.Component;

@Component("webscript.eu.xenit.alfred.api.rest.v1.nodes.upload.post")
public class UploadWebscript extends AbstractWebScript {
    private final static Logger logger = LoggerFactory.getLogger(UploadWebscript.class);

    private static final String MULTIPART_FORMDATA = "multipart/form-data";

    private final ServiceRegistry serviceRegistry;

    private final INodeService nodeService;

    private final IPermissionService permissionService;

    private final IFileFolderService fileFolderService;

    private final ObjectMapper objectMapper;

    public UploadWebscript(ServiceRegistry serviceRegistry,
                           INodeService nodeService,
                           IPermissionService permissionService,
                           IFileFolderService fileFolderService) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.fileFolderService = fileFolderService;
        this.objectMapper = ObjectMapperFactory.getNewObjectMapper();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        if (!MULTIPART_FORMDATA.equals(req.getContentType()))
            logger.debug("Was expecting enc type {}, don't think {} will work", MULTIPART_FORMDATA, req.getContentType());
        FormData formData = (FormData) req.parseContent();
        final FormData.FormField[] fields = formData.getFields();

        if (fields.length < 1) {
            String message = "There are no form fields!";
            logger.error(message);
            throw new IllegalStateException(message);
        }

        res.setContentType("application/json");
        try {
            final NodeInfo nodeInfo = uploadNode(fields);
            res.setStatus(Status.STATUS_OK);
            res.getWriter().write(objectMapper.writeValueAsString(nodeInfo));
        } catch (IOException e) {
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.getWriter().write(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.debug("Not Authorized", e);
            res.setStatus(Status.STATUS_FORBIDDEN);
            res.getWriter().write("Not authorised to execute this operation");
        } catch (FileExistsException e) {
            String message = "File already exists";
            logger.debug(message, e);
            res.setStatus(Status.STATUS_BAD_REQUEST);
            res.getWriter().write(message);
        }
    }

    public NodeInfo uploadNode(FormData.FormField[] fields) throws IOException {
        String type = null;
        String parent = null;
        Boolean extractMetadata = null;
        MetadataChanges metadata = null;
        FormData.FormField content = null;

        // locate file attributes
        for (FormData.FormField field : fields) {
            final String fieldName = field.getName();
            if ("parent".equals(fieldName)) {
                parent = field.getValue();
            } else if ("file".equals(fieldName) && field.getIsFile()) {
                content = field;
            } else if ("extractMetadata".equals(fieldName)) {
                extractMetadata = Boolean.valueOf(field.getValue());
            } else if ("type".equals(fieldName)) {
                type = field.getValue();
            } else if ("metadata".equals(fieldName) && !Strings.isNullOrEmpty(field.getContent().getContent())) {
                metadata = objectMapper.readValue(field.getContent().getContent(), MetadataChanges.class);
            }
        }

        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        // Note the difference between:
        //  * metadata        = The metadata the user annotates the file with.
        //  * extractMetadata = Whether the users want metadata automatically extracted from the file.
        // Both setting metadata and extracting metadata are optional.
        // They can happen (or not) independently of each other.
        type = type == null ? ContentModel.TYPE_CONTENT.toString() : type;
        extractMetadata = Boolean.TRUE.equals(extractMetadata);

        if (content == null) {
            throw new IllegalArgumentException("Content must be supplied as a multipart 'file' field");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Must supply a 'parent' field");
        }

        final String finalParent = parent;
        final String finalType = type;
        final MetadataChanges finalMetadata = metadata;
        final Boolean finalExtractMetadata = extractMetadata;
        final FormData.FormField finalContent = content;
        eu.xenit.alfred.api.data.NodeRef resultRef;
        try {
            resultRef = transactionHelper
                    .doInTransaction(() -> createNodeForUpload(finalParent, finalContent.getFilename(),
                            finalContent.getInputStream(), finalType, finalMetadata,
                            finalExtractMetadata), false, true);
        } catch (org.alfresco.service.cmr.model.FileExistsException fileExistsException) {
            throw new FileExistsException(
                    null,
                    new eu.xenit.alfred.api.data.NodeRef(fileExistsException.getParentNodeRef().toString()),
                    fileExistsException.getName());
        }
        return AlfredApiV1Webscript.nodeRefToNodeInfo(resultRef, fileFolderService, nodeService, permissionService);
    }


    public eu.xenit.alfred.api.data.NodeRef createNodeForUpload(String finalParent,
                                                                String originalFileName,
                                                                InputStream contentStream,
                                                                String finalType,
                                                                MetadataChanges finalMetadata,
                                                                Boolean finalExtractMetadata) {
        eu.xenit.alfred.api.data.NodeRef newNode = nodeService
                .createNode(new eu.xenit.alfred.api.data.NodeRef(finalParent), originalFileName,
                        new eu.xenit.alfred.api.data.QName(finalType));
        nodeService.setContent(newNode, contentStream, originalFileName);

        if (finalMetadata != null) {
            nodeService.setMetadata(newNode, finalMetadata);
        }

        if (Boolean.TRUE.equals(finalExtractMetadata)) {
            try {
                nodeService.extractMetadata(newNode);
                logger.debug("Metadata extracted");
            } catch (Exception ex) {
                logger.warn("Exception while extracting metadata", ex);
            }
        }
        return newNode;
    }
}
