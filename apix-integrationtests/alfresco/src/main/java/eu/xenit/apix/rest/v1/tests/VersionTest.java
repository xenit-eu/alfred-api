package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kenneth on 16.03.16.
 */
public class VersionTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(VersionTest.class);

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testVersionGet() throws IOException {

        String url = makeAlfrescoBaseurlAdmin() + "/apix/v1/version";
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();

        String result = EntityUtils.toString(httpResponse.getEntity());
        logger.debug(" result: " + result);

        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertTrue(result.contains("\"version\":"));
        assertTrue(result.contains("\"major\":"));
        assertTrue(result.contains("\"minor\":"));
        assertTrue(result.contains("\"patch\":"));
    }
}
