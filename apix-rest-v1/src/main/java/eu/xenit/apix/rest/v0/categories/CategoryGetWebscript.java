package eu.xenit.apix.rest.v0.categories;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.rest.v0.RestV0Config;
import java.io.IOException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 01/12/2015.
 */
@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Component("eu.xenit.apix.rest.v0.categories.CategoryGetWebscript")
@Authentication(AuthenticationType.USER)
public class CategoryGetWebscript extends AbstractWebScript {

    @Autowired
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Uri("/eu/xenit/category/{noderef}/subcategories")
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        NodeRef nodeRef = new NodeRef(req.getParameter("noderef"));


    }
}
