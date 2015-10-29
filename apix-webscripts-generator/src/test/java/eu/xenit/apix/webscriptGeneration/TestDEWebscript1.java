package eu.xenit.apix.webscriptGeneration;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
@WebScript(baseUri = "/base", families = "My Family")
@Authentication(AuthenticationType.GUEST)
public class TestDEWebscript1 {

    @Uri("/method")
    public void testGet() {

    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/method/{param}", method = HttpMethod.POST)
    public void testPost() {

    }
}
