package eu.xenit.apix.javadoc;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FormatStyle;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.TransactionType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.support.AbstractBundleResourceHandler;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;


/**
 * Created by jasper on 27/09/17.
 */

@Component
@WebScript(families = "apix")
@Transaction(TransactionType.NONE)
public class JavadocResourcesWebscript extends AbstractBundleResourceHandler {

    private final String packagePath;

    public JavadocResourcesWebscript() {
        packagePath = this.getClass().getPackage().getName().replace('.', '/');
    }


    @Uri(value = "/apix/javadocs/{path}", formatStyle = FormatStyle.ARGUMENT)
    public void handleResources(@UriVariable final String path, final WebScriptResponse response,
            WebScriptRequest request) throws Exception {
        handleResource(path, request, response);
        //        final URL resource = getBundleContext().getBundle().getEntry(path);
        //        if (resource != null) {
        //            sendResource(request, response, resource);
        //        } else {
        //            handleResourceNotFound(path, response);
        //        }

    }

    /* Utility operations */

    @Override
    protected String getBundleEntryPath(final String path) {
        return String.format("%s/%s", "/javadoc", path);
    }

    @Override
    protected void initContentTypes() {
        super.initContentTypes();
        getContentTypesByExtension().put("pdf", "application/pdf");
        getContentTypesByExtension().put("js", "text/javascript");
        getContentTypesByExtension().put("css", "text/css");
        getContentTypesByExtension().put("html", "text/html");
        getContentTypesByExtension().put("otf", "application/x-font-otf");
        getContentTypesByExtension().put("eot", "application/vnd.ms-fontobject");
        getContentTypesByExtension().put("svg", "image/svg+xml");
        getContentTypesByExtension().put("ttf", "application/x-font-ttf");
        getContentTypesByExtension().put("woff", "application/x-font-woff");
        getContentTypesByExtension().put("woff2", "application/x-font-woff");
        getContentTypesByExtension().put("class", "text/html");


    }


}
