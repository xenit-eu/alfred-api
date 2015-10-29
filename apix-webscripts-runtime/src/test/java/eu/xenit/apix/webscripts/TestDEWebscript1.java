package eu.xenit.apix.webscripts;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
@WebScript(baseUri = "/base", families = "My Family")
@Authentication(AuthenticationType.GUEST)
public class TestDEWebscript1 {

    static int count = 0;

    @Uri("/method")
    public void testGet() {
        count++;
        System.out.println("testGet!");
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/method/{param}", method = HttpMethod.POST)
    public void testPost() {
        count++;
        System.out.println("testPost!");
    }
}
