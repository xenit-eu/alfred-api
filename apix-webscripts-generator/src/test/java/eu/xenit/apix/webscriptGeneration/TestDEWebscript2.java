package eu.xenit.apix.webscriptGeneration;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
//@Component
@WebScript(baseUri = "/base2", families = "My Family")
public class TestDEWebscript2 {

    @Uri("/")
    public void Test() {

    }
}
