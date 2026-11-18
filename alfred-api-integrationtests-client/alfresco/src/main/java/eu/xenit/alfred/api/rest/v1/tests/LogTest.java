package eu.xenit.alfred.api.rest.v1.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.client.fluent.Request;
import org.junit.Before;
import org.junit.Test;

/**
 * GET /apix/v1/tmp/log is restricted to administrators.
 * <p>
 * Only access control is tested: the log file path inherited from the MVC implementation
 * ("$CATALINA_HOME/logs/catalina.out", which Spring does not resolve) does not exist in the test environment, so
 * administrator requests currently fail with HTTP 500, exactly as before the migration.
 */
public class LogTest extends RestV1BaseTest {

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Same idempotent user creation as RestV1BaseTest.init(), without creating its test folders.
        transactionHelper.doInTransaction(() -> createUser(USERWITHOUTRIGHTS, USERWITHOUTRIGHTS, USERWITHOUTRIGHTS,
                USERWITHOUTRIGHTS_EMAIL), false, true);
    }

    @Test
    public void nonAdminIsUnauthorized() throws IOException {
        String url = makeAlfrescoBaseurl(USERWITHOUTRIGHTS, USERWITHOUTRIGHTS) + "/apix/v1/tmp/log?lines=1";
        assertEquals(401, Request.Get(url).execute().returnResponse().getStatusLine().getStatusCode());
    }

    @Test
    public void anonymousIsUnauthorized() throws IOException {
        String url = makeAlfrescoBaseurl() + "/apix/v1/tmp/log?lines=1";
        assertEquals(401, Request.Get(url).execute().returnResponse().getStatusLine().getStatusCode());
    }
}
