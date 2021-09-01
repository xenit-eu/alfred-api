package eu.xenit.apix.rest.v1.categories;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import eu.xenit.apix.categories.Category;
import eu.xenit.apix.categories.ICategoryService;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.rest.v1.RestV1Config;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@WebScript(baseUri = RestV1Config.BaseUrl + "/category", families = RestV1Config.Family, defaultFormat = "json",
        description = "Retrieves Category information", value = "Category")
@Authentication(AuthenticationType.USER)
@Component("eu.xenit.apix.rest.v1.categories.CategoryWebScript1")
public class CategoryWebScript1 extends ApixV1Webscript {

    Logger logger = LoggerFactory.getLogger(CategoryWebScript1.class);
    @Autowired
    private ICategoryService categoryService;

    @Uri(value = "/aspect/{qname}", method = HttpMethod.GET)
    @ApiOperation(value = "Return the categories available for an aspect", notes = "")
    @ApiResponses(@ApiResponse(code = 200, message = "Success", response = Categories.class))
    public void getCategoriesForAspect(@UriVariable final String qname, WebScriptResponse webScriptResponse)
            throws IOException {
        QName apixQName = new QName(qname);
        List<Category> categories = categoryService.getCategoryTree(apixQName);
        writeJsonResponse(webScriptResponse, new Categories(categories));
    }
}
