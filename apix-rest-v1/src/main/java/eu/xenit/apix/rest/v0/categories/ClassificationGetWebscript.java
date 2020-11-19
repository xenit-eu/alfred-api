package eu.xenit.apix.rest.v0.categories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.categories.ICategoryService;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.rest.v0.RestV0Config;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 01/12/2015.
 */
@WebScript(families = {RestV0Config.Family}, defaultFormat = "json")
@Component("eu.xenit.apix.rest.v0.categories.ClassificationGetWebscript")
@Authentication(AuthenticationType.USER)
public class ClassificationGetWebscript extends AbstractWebScript {

    @Autowired
    private ICategoryService catService;

    @Uri("/eu/xenit/classification/{aspectqname}")
    @Override()
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        QName aspectQname = new QName(req.getServiceMatch().getTemplateVars().get("aspectqname"));

        ObjectMapper m = new ObjectMapper();
        String ret = m.writeValueAsString(catService.getCategoryTree(aspectQname));

        res.getWriter().write(ret);


    }
}
