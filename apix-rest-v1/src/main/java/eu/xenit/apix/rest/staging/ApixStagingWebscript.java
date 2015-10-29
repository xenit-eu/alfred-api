package eu.xenit.apix.rest.staging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.filefolder.IFileFolderService;
import eu.xenit.apix.node.*;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.permissions.PermissionValue;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import org.alfresco.repo.admin.SysAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApixStagingWebscript {

    private final static Logger logger = LoggerFactory.getLogger(ApixStagingWebscript.class);

    protected void writeJsonResponse(WebScriptResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setContentEncoding("utf-8");
        response.setHeader("Cache-Control", "no-cache");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(response.getWriter(), object);
    }
}
